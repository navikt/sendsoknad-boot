package no.nav.modig.security.sts.client;

import no.nav.sbl.dialogarena.tokensupport.TokenUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.tokenstore.TokenStore;
import org.apache.cxf.ws.security.tokenstore.TokenStoreFactory;
import org.apache.cxf.ws.security.trust.STSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NAVSTSClient extends STSClient {
    private static final Logger logger = LoggerFactory.getLogger(NAVSTSClient.class);
    private static TokenStore tokenStore;

    public NAVSTSClient(Bus b) {
        super(b);
    }

    @Override
    protected boolean useSecondaryParameters() {
        return false;
    }

    @Override
    public SecurityToken requestSecurityToken(String appliesTo, String action, String requestType, String binaryExchange) throws Exception {

        String key = chooseCachekey();
        ensureTokenStoreExists();

        SecurityToken token = tokenStore.getToken(key);
        if (token == null) {
            logger.debug("Missing token for {}, fetching it from STS", key);
            token = super.requestSecurityToken(appliesTo, action, requestType, binaryExchange);
            tokenStore.add(key, token);
        } else {
            logger.debug("Retrived token for {} from tokenStore", key);
        }
        return token;
    }

    private void ensureTokenStoreExists() {
        if (tokenStore == null) {
            createTokenStore();
        }
    }

    private synchronized void createTokenStore() {
        logger.debug("Creating tokenStore");
        if (tokenStore == null) {
            try {
                tokenStore = TokenStoreFactory.newInstance().newTokenStore(SecurityConstants.TOKEN_STORE_CACHE_INSTANCE, message);
            } catch (Exception e) {
                logger.error("Caught exception when creating TokenStore", e);
            }
        }
    }

    private String chooseCachekey() {

        // choose cachekey based on IdentType
        String key;
        if (!StringUtils.isEmpty(TokenUtils.getSubject())) {
            if (TokenUtils.hasTokenForIssuer(TokenUtils.ISSUER_OPENAM)) {
                key = TokenUtils.getSubject() + "-" + TokenUtils.ISSUER_OPENAM + "-" + "Level4";
            } else if (TokenUtils.hasTokenForIssuer(TokenUtils.ISSUER_LOGINSERVICE)) {
                key = TokenUtils.getSubject() + "-" + TokenUtils.ISSUER_LOGINSERVICE + "-" + "Level4";
            } else {
                throw new RuntimeException("No suitable token for either issuer OpenAM or Loginservice found. Unable to perform external call.");
            }
        } else {
            key = "systemSAML";
        }
        logger.debug("Chosen cackekey for this request is {}", key);
        return key;
    }
}
