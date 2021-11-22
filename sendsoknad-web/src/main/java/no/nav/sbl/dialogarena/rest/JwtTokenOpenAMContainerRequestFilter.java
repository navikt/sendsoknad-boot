package no.nav.sbl.dialogarena.rest;



import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import no.nav.sbl.dialogarena.tokensupport.TokenUtils;
import no.nav.security.token.support.core.exceptions.JwtTokenInvalidClaimException;
import no.nav.security.token.support.core.validation.JwtTokenAnnotationHandler;
import no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder;

@Provider
public class JwtTokenOpenAMContainerRequestFilter implements ContainerRequestFilter {

    private final JwtTokenAnnotationHandler jwtTokenAnnotationHandler;

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    public JwtTokenOpenAMContainerRequestFilter() {
        this.jwtTokenAnnotationHandler = new JwtTokenAnnotationHandler(JaxrsTokenValidationContextHolder.getHolder());
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        if (TokenUtils.hasTokenForIssuer(TokenUtils.ISSUER_OPENAM)) {
            return;
        }
        Method method = resourceInfo.getResourceMethod();
        try {
            jwtTokenAnnotationHandler.assertValidAnnotation(method);
        } catch (JwtTokenInvalidClaimException e) {
            throw new WebApplicationException(e, Response.Status.FORBIDDEN);
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.UNAUTHORIZED);
        }
    }
}

