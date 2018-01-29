package dk.kontentsu.api.exceptionmappers;

import javax.persistence.NoResultException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import dk.kontentsu.api.exposure.model.ErrorRepresentation;
import dk.kontentsu.exception.ErrorCode;

/**
 * Exception mapper for JPA no result exception.
 *
 * @author Jens Borch Christiansen
 */
public class NoResultExceptionMapper implements ExceptionMapper<NoResultException> {

    @Override
    public Response toResponse(final NoResultException e) {
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(new ErrorRepresentation(ErrorCode.NOT_FOUND_ERROR, e.getMessage()))
                .build();
    }

}
