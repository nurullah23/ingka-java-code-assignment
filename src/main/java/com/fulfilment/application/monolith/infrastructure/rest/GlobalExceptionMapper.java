package com.fulfilment.application.monolith.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Inject
    ObjectMapper objectMapper;

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        LOGGER.error("An error occurred during request processing", exception);

        int code = 500;
        if (exception instanceof WebApplicationException) {
            code = ((WebApplicationException) exception).getResponse().getStatus();
        } else if (exception instanceof IllegalArgumentException) {
            code = 400;
        } else if (exception instanceof IllegalStateException) {
            code = 409;
        }

        ObjectNode exceptionJson = objectMapper.createObjectNode();
        exceptionJson.put("exceptionType", exception.getClass().getName());
        exceptionJson.put("code", code);

        if (exception.getMessage() != null) {
            exceptionJson.put("error", exception.getMessage());
        }

        return Response.status(code).entity(exceptionJson).build();
    }
}
