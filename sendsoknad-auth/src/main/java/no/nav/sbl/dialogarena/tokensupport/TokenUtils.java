package no.nav.sbl.dialogarena.tokensupport;

import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.security.LoginService;

import com.fasterxml.jackson.databind.deser.std.EnumSetDeserializer;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.core.domain.IdentType;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder;

public class TokenUtils {
    
        private static TokenValidationContextHolder contextHolder = JaxrsTokenValidationContextHolder.getHolder();
        
        public static final String ISSUER_LOGINSERVICE = "loginservice";
        public static final String ISSUER_TOKENX ="tokenx";
        public static final String ISSUER_OPENAM = "openam";
    
        public static enum AUTH_TYPE { OPENAM,LOGINSERVICE, TOKENX };
    
        public static List<AUTH_TYPE>  issuersByAvailableTokens() {
            List<AUTH_TYPE> authType = new ArrayList<TokenUtils.AUTH_TYPE>();
            if (SubjectHandler.getSubjectHandler() !=null && SubjectHandler.getSubjectHandler().getEksternSsoToken()!=null) {
                authType.add(AUTH_TYPE.OPENAM);
            }
            TokenValidationContext context = contextHolder.getTokenValidationContext();
            if (context.hasTokenFor(ISSUER_LOGINSERVICE)) {
                authType.add(AUTH_TYPE.LOGINSERVICE);
            }
            if (context.hasTokenFor(ISSUER_LOGINSERVICE)) {
                authType.add(AUTH_TYPE.TOKENX);
            }
           return authType;
        }
        
        private static TokenValidationContext getTokenValidationContext() {
            return contextHolder.getTokenValidationContext();
        }
        
        public static boolean hasTokenForIssuer(String issuer) {
            if (issuer == ISSUER_OPENAM) {
                return SubjectHandler.getSubjectHandler()!=null && ! StringUtils.isEmpty(SubjectHandler.getSubjectHandler().getEksternSsoToken());
            }
            else {
                TokenValidationContext context = contextHolder.getTokenValidationContext();
                return context.hasTokenFor(issuer);
            }
        }
        
        public static String getSubject() {
            if (SubjectHandler.getSubjectHandler().getIdentType() == IdentType.EksternBruker) {
                return SubjectHandler.getSubjectHandler().getUid();
            }
            TokenValidationContext context = contextHolder.getTokenValidationContext();
            if (context.hasTokenFor(ISSUER_LOGINSERVICE)) {
                return context.getJwtToken(ISSUER_LOGINSERVICE).getSubject();
            }
            if (context.hasTokenFor(ISSUER_TOKENX)) {
                return context.getJwtToken(ISSUER_TOKENX).getSubject();
            }
            return null;
        }
        
        public static String getTokenAsString(String issuer) {
            TokenValidationContext context = contextHolder.getTokenValidationContext();
            
            switch (issuer) {
                case  ISSUER_OPENAM: return SubjectHandler.getSubjectHandler().getEksternSsoToken();
            
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
