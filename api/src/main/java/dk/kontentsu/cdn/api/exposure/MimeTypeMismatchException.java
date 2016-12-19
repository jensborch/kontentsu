package dk.kontentsu.cdn.api.exposure;

import javax.ws.rs.core.Response;

import dk.kontentsu.cdn.api.ApiErrorException;
import dk.kontentsu.exception.ErrorCode;

/**
 *
 * @author Jens Borch Christiansen
 */
public class MimeTypeMismatchException extends ApiErrorException {

    private static final long serialVersionUID = 1L;

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
