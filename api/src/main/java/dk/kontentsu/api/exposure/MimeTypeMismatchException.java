package dk.kontentsu.api.exposure;

import javax.ws.rs.core.Response;

import dk.kontentsu.exception.ErrorCode;

/**
 * Thrown MIME type for requested content is different from what is stored in Kontentsu.
 *
 * @author Jens Borch Christiansen
 */
public class MimeTypeMismatchException extends ApiErrorException {

    private static final long serialVersionUID = -8195474150691319982L;

    public MimeTypeMismatchException(final String message) {
        super(message);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.MIME_TYPE_MISMATCH_ERROR;
    }

    @Override
    public Response.Status getHttpErrorCode() {
        return Response.Status.NOT_ACCEPTABLE;
    }



}
