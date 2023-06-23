package no.nav.sbl.dialogarena.rest.feil;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.SikkerhetsBegrensningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.serverError;
import static javax.ws.rs.core.Response.status;
import static no.nav.sbl.dialogarena.rest.feil.Feilmelding.NO_BIGIP_5XX_REDIRECT;

@Provider
public class ApplicationExceptionMapper implements ExceptionMapper<SendSoknadException> {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationExceptionMapper.class);

    @Override
    public Response toResponse(SendSoknadException e) {
        Response.ResponseBuilder response;
        if (e instanceof UgyldigOpplastingTypeException) {
            response = status(UNSUPPORTED_MEDIA_TYPE);
            logger.warn("Feilet opplasting: {}", e.getMessage(), e);
        } else if (e instanceof OpplastingException) {
            response = status(REQUEST_ENTITY_TOO_LARGE);
            logger.warn("Feilet opplasting: {}", e.getMessage(), e);
        } else if (e instanceof AuthorizationException) {
            response = status(FORBIDDEN);
            logger.warn("Ikke tilgang til ressurs: {}", e.getMessage(), e);
            return response.type(APPLICATION_JSON).entity(new Feilmelding(e.getId(), "Ikke tilgang til ressurs")).build();
        } else if (e instanceof SikkerhetsBegrensningException) {
            response = status(FORBIDDEN);
            logger.warn("Sikkerhetsbegrensning: {}", e.getMessage(), e);
            return response.type(APPLICATION_JSON).entity(new Feilmelding(e.getId(), "Ikke tilgang til ressurs")).build();
        }
        else if (e instanceof IkkeFunnetException) {
            response = status(NOT_FOUND);
            logger.warn("Fant ikke ressurs: {}", e.getMessage(), e);
        } else if (e instanceof AlleredeHandtertException) {
            response = serverError().header(NO_BIGIP_5XX_REDIRECT, true);
        } else if (e instanceof SoknadCannotBeChangedException) {
            response = status(METHOD_NOT_ALLOWED);
            logger.warn("Søknad {} kan ikke endres da den er innsendt eller slettet");
        } else if (e instanceof  ClientErrorException) {
            response = status(BAD_REQUEST);
            logger.warn("REST-kall feilet. Feilkode: {}, {}", e.getId(), e.getMessage(), e);
            return response.type(APPLICATION_JSON).entity(new Feilmelding(e.getId(), "Ugyldig status på søknad")).build();
        }else {
            response = serverError().header(NO_BIGIP_5XX_REDIRECT, true);
            logger.error("REST-kall feilet: {}", e.getMessage(), e);
        }

        // Mediatypen kan settes til APPLICATION_JSON når vi ikke trenger å støtte IE9 lenger.
        return response.type(TEXT_PLAIN).entity(new Feilmelding(e.getId(), e.getMessage())).build();
    }
}
