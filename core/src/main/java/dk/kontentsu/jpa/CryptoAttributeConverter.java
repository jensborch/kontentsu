/*
 * The MIT License
 *
 * Copyright 2016 Jens Borch Christiansen.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dk.kontentsu.jpa;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import dk.kontentsu.configuration.Config;

/**
 * JPA converter for encrypting columns.
 *
 * @author Jens Borch Christiansen
 */
@Converter
public class CryptoAttributeConverter implements AttributeConverter<String, String> {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String KEY_ALGORITHM = "AES";
    private static final Charset ENCODING = StandardCharsets.UTF_8;
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SECRET_KEY_ITERATION_COUNT = 65536;
    private static final int SECRET_KEY_KEY_LENGTH = 128;
    private static final IvParameterSpec IV_PARAMETERS = new IvParameterSpec(new byte[]{12, 42, 14, 78, 91, 77, 65, 42, 14, 22, 13, 78, 99, 77, 65, 42});
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Inject
    private Config conf;

    private byte[] getNextSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return salt;
    }

    public byte[] concat(final byte[] salt, final byte[] encoded) {
        int encodedLength = encoded.length;
        byte[] result = new byte[SALT_LENGTH + encodedLength];
        System.arraycopy(salt, 0, result, 0, SALT_LENGTH);
        System.arraycopy(encoded, 0, result, SALT_LENGTH, encodedLength);
        return result;
    }

    private byte[] getSalt(final byte[] columnData) {
        byte[] salt = new byte[SALT_LENGTH];
        System.arraycopy(columnData, 0, salt, 0, SALT_LENGTH);
        return salt;
    }

    private byte[] getEncrypted(final byte[] columnData) {
        byte[] encrypted = new byte[columnData.length - SALT_LENGTH];
        System.arraycopy(columnData, SALT_LENGTH, encrypted, 0, columnData.length - SALT_LENGTH);
        return encrypted;
    }

    private SecretKey getEncryptionKey(final byte[] salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
        KeySpec spec = new PBEKeySpec(conf.dbEncryptionKey().toCharArray(), salt, SECRET_KEY_ITERATION_COUNT, SECRET_KEY_KEY_LENGTH);
        return factory.generateSecret(spec);
    }

    @Override
    public String convertToDatabaseColumn(final String sensitive) {
        LOGGER.debug("Encrypting database column using algorithm {}", ALGORITHM);
        try {
            byte[] salt = getNextSalt();
            Key key = new SecretKeySpec(getEncryptionKey(salt).getEncoded(), KEY_ALGORITHM);
            Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(Cipher.ENCRYPT_MODE, key, IV_PARAMETERS);
            return new String(Base64.getEncoder().encode(concat(salt, c.doFinal(sensitive.getBytes(ENCODING)))), ENCODING
            );
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | InvalidKeySpecException | NoSuchAlgorithmException
                | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new CryptoException("Error encrypting database column", e);
        }
    }

    @Override
    public String convertToEntityAttribute(final String sensitive) {
        LOGGER.debug("Decrypting database column using algorithm {}", ALGORITHM);
        try {
            byte[] columnData = Base64.getDecoder().decode(sensitive.getBytes(ENCODING));
            Key key = new SecretKeySpec(getEncryptionKey(getSalt(columnData)).getEncoded(), KEY_ALGORITHM);
            Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(Cipher.DECRYPT_MODE, key, IV_PARAMETERS);
            return new String(c.doFinal(getEncrypted(columnData)), ENCODING);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | InvalidKeySpecException | NoSuchAlgorithmException
                | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new CryptoException("Error decrypting database column", e);
        }
    }
}
