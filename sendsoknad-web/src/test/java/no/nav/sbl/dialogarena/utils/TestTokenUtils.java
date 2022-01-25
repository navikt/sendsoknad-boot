package no.nav.sbl.dialogarena.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

import no.nav.sbl.dialogarena.tokensupport.TokenUtils;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.validation.JwtTokenValidatorFactory;
import no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder;

public class TestTokenUtils {
    
    public static String SOME_DEFAULT_FNR = "***REMOVED***";
    
    private static JwtToken jwtToken(String issuer,String subject) {
        PlainJWT plainJWT = new PlainJWT(new JWTClaimsSet.Builder()
            .issuer(issuer)
            .subject(subject)
            .build());
        return new JwtToken(plainJWT.serialize());
    }
    
    
    
    public static TokenValidationContext mockValidationContext (String fnr) {
        Map<String, JwtToken> map = new HashMap<>();
        TokenValidationContext tokenValidationContext = new TokenValidationContext(map);   
        map.put("loginservice", jwtToken("https://testloginservice", fnr));
        return tokenValidationContext;
    }
    
    public static void setSecurityContext() throws Exception {
        
        setSecurityContext(SOME_DEFAULT_FNR);
        
    }
    
    @SuppressWarnings("unchecked")
    public static void setSecurityContext(String fnr) throws Exception {
            Field threadLocalHolder = JaxrsTokenValidationContextHolder.class.getDeclaredField("validationContextHolder");
            threadLocalHolder.setAccessible(true);
            ThreadLocal<TokenValidationContext> validationContext = (ThreadLocal<TokenValidationContext>)threadLocalHolder.get(null);
            validationContext.set(mockValidationContext(fnr));
       
          
    }

}
