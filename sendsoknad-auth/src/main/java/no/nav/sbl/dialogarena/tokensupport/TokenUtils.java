package no.nav.sbl.dialogarena.tokensupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import no.nav.modig.common.SpringContextAccessor;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder;

public class TokenUtils {
    
        private static TokenValidationContextHolder contextHolder = JaxrsTokenValidationContextHolder.getHolder();
        
        public static final String ISSUER_TOKENX ="tokenx";
        public static final String FSS_PROXY_AUTHORIZATION_HEADER = "x-fss-proxy-authorization";
        public static final String ACR_LEVEL4 = "acr=Level4";
        public static final String ACR_IDPORTEN_LOA_HIGH = "acr=idporten-loa-high";
        

    
        public static enum AUTH_TYPE { TOKENX };
    
        public static List<AUTH_TYPE>  issuersByAvailableTokens() {
            List<AUTH_TYPE> authType = new ArrayList<TokenUtils.AUTH_TYPE>();
          
            TokenValidationContext context = contextHolder.getTokenValidationContext();
            if (context.hasTokenFor(ISSUER_TOKENX)) {
                authType.add(AUTH_TYPE.TOKENX);
            }
           return authType;
        }
        
       
        public static boolean hasTokenForIssuer(String issuer) {
           
           return contextHolder.getTokenValidationContext() != null ? contextHolder.getTokenValidationContext().hasTokenFor(issuer) : false; 
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
            else {
                return null;
            }
        }
        
        public static String getTokenAsString(String issuer) {
            TokenValidationContext context = contextHolder.getTokenValidationContext();
            
            switch (issuer) {
              
                case  ISSUER_TOKENX : {
                                        if (!context.hasTokenFor(issuer)) {
                                            throw  new RuntimeException("No valid token for issuer: " + issuer );
                                        }
                                        return context.getJwtToken(issuer).getTokenAsString() ;
                                      }
            
             default: throw new RuntimeException("Unknown issuer:" + issuer);
            }
            
        }
        
        public static Supplier<Map<String, List<String>>> proxyHeaderSupplier() {
            return () -> {
                // Dersom det ikke er en Spring-Context, f.eks ved tester, skal ikke interceptoren settes.
                 
                if (SpringContextAccessor.hasContext()) {
                    var serviceName = TokenUtils.hasTokenForIssuer(TokenUtils.ISSUER_TOKENX) ? "SoknadFSSTokenX"  : "SoknadFSSAzureAD";
                    
                    var tokenService = SpringContextAccessor.getBean(serviceName,TokenService.class);
                    return Map.of(FSS_PROXY_AUTHORIZATION_HEADER, List.of("Bearer " + tokenService.getToken()));
                }
                return Collections.emptyMap();
            };
        }
}
