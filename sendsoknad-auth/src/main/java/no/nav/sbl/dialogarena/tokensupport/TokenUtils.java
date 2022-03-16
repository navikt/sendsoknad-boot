package no.nav.sbl.dialogarena.tokensupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder;

public class TokenUtils {
    
        private static TokenValidationContextHolder contextHolder = JaxrsTokenValidationContextHolder.getHolder();
        
        public static final String ISSUER_LOGINSERVICE = "loginservice";
        public static final String ISSUER_TOKENX ="tokenx";

    
        public static enum AUTH_TYPE { LOGINSERVICE, TOKENX };
    
        public static List<AUTH_TYPE>  issuersByAvailableTokens() {
            List<AUTH_TYPE> authType = new ArrayList<TokenUtils.AUTH_TYPE>();
          
            TokenValidationContext context = contextHolder.getTokenValidationContext();
            if (context.hasTokenFor(ISSUER_LOGINSERVICE)) {
                authType.add(AUTH_TYPE.LOGINSERVICE);
            }
            if (context.hasTokenFor(ISSUER_LOGINSERVICE)) {
                authType.add(AUTH_TYPE.TOKENX);
            }
           return authType;
        }
        
       
        public static boolean hasTokenForIssuer(String issuer) {
           
           TokenValidationContext context = contextHolder.getTokenValidationContext();
           if (contextHolder.getTokenValidationContext() == null) {
               return false;
           }
           return context.hasTokenFor(issuer);
            
        }
        
        private static String getSubject(JwtToken token) {
             JwtTokenClaims claims = token.getJwtTokenClaims();
            
             Optional<String> fnr = Optional.of( claims.getStringClaim("pid") != null ? claims.getStringClaim("pid") : claims.getStringClaim("sub"));
             
             return fnr.orElseThrow(() -> new RuntimeException("Missing user claim"));
        }
        
        public static String getSubject() {
            
            TokenValidationContext context = contextHolder.getTokenValidationContext();
            if (context==null) {
                return null;
            }
            else if (context.hasTokenFor(ISSUER_TOKENX)) {
                return getSubject( context.getJwtToken(ISSUER_TOKENX) );
            }
            else if ( context.hasTokenFor(ISSUER_LOGINSERVICE) ) {
                return getSubject(context.getJwtToken(ISSUER_LOGINSERVICE));
            }
            else {
                return null;
            }
        }
        
        public static String getTokenAsString(String issuer) {
            TokenValidationContext context = contextHolder.getTokenValidationContext();
            
            switch (issuer) {
              
                case  ISSUER_LOGINSERVICE :
                case  ISSUER_TOKENX : {
                                        if (!context.hasTokenFor(issuer)) {
                                            throw  new RuntimeException("No valid token for issuer: " + issuer );
                                        }
                                        return context.getJwtToken(issuer).getTokenAsString() ;
                                      }
            
             default: throw new RuntimeException("Unknown issuer:" + issuer);
            }
            
        }
}
