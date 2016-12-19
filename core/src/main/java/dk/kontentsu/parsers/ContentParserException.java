package dk.kontentsu.parsers;

import dk.kontentsu.exception.ApplicationException;
import dk.kontentsu.exception.ErrorCode;

/**
 * Thrown when an error occurs parsing the content uploaded - e.g. if hal+json content isn't formated correctly.
 *
 * @author Jens Borch Christiansen
 */
public class ContentParserException extends ApplicationException {

    private static final long serialVersionUID = 1320189607812212864L;

    public ContentParserException(final String msg, final Exception ex) {
        super(msg, ex);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.CONTENT_PARSER_ERROR;
    }

}
