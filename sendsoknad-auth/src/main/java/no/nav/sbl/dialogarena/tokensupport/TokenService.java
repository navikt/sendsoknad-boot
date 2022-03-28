package no.nav.sbl.dialogarena.tokensupport;

import no.nav.modig.common.SpringContextAccessor;
import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TokenService {
    private final ClientProperties clientProperties;
    private final OAuth2AccessTokenService oAuth2AccessTokenService;
    

    public TokenService(ClientProperties clientProperties, OAuth2AccessTokenService oAuth2AccessTokenService) {
        this.clientProperties = clientProperties;
        this.oAuth2AccessTokenService = oAuth2AccessTokenService;
    }

    public String getToken() {
        var response = oAuth2AccessTokenService.getAccessToken(clientProperties);
        return response.getAccessToken();
    }

}
