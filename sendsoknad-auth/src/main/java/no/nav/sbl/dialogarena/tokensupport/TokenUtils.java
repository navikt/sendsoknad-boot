package no.nav.sbl.dialogarena.tokensupport;

import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder;

public class TokenUtils {
	
	public static String getFoedselsnummer() {
		
		TokenValidationContext context = JaxrsTokenValidationContextHolder.getHolder().getTokenValidationContext();
		String foedselsnummerFraLoginService  = getFoedselsnummerFraLoginService();
		
		if (foedselsnummerFraLoginService != null) {
			return foedselsnummerFraLoginService;
		}
		else if (context != null && context.hasValidToken()) {
		    JwtToken token = context.getJwtToken("default");
		    return token.getSubject();
		   
		}
		else {
			return SubjectHandler.getSubjectHandler().getUid();
		}
	}
	
	public static String getTokenAsString() {
		
		TokenValidationContext context = JaxrsTokenValidationContextHolder.getHolder().getTokenValidationContext();
		if (context != null && context.hasValidToken()) {
		    JwtToken token = context.getJwtToken("default");
		    return token.getTokenAsString();
		   
		}
		else {
			return SubjectHandler.getSubjectHandler().getEksternSsoToken();
		}
	}
	
	public static String getFoedselsnummerFraLoginService() {
		
		TokenValidationContext context = JaxrsTokenValidationContextHolder.getHolder().getTokenValidationContext();
		if (context != null && context.hasValidToken()) {
		    JwtToken token = context.getJwtToken("loginservice");
		    return token.getSubject();
		   
		}
		return null;
	}
	
	public static String getTokenAsStringFraLoginService() {
		
		TokenValidationContext context = JaxrsTokenValidationContextHolder.getHolder().getTokenValidationContext();
		if (context != null && context.hasValidToken()) {
		    JwtToken token = context.getJwtToken("loginservice");
		    return token.getTokenAsString();
		   
		}
		return null;
	}
	
	
	
	public static boolean hasUserContext() {
	
		TokenValidationContext context = JaxrsTokenValidationContextHolder.getHolder().getTokenValidationContext();
		if (context != null && context.hasValidToken()) {
		    JwtToken token = context.getJwtToken("default");
		    return true;
		}
		else {
			return getSubjectHandler().getIdentType() != null;
		}
		
	}
	
	

}
