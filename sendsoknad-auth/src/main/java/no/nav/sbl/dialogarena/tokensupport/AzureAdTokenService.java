package no.nav.sbl.dialogarena.tokensupport;

import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;

public class AzureAdTokenService {
    private final ClientProperties clientProperties;
    private final OAuth2AccessTokenService oAuth2AccessTokenService;

    public AzureAdTokenService(ClientProperties clientProperties, OAuth2AccessTokenService oAuth2AccessTokenService) {
        this.clientProperties = clientProperties;
        this.oAuth2AccessTokenService = oAuth2AccessTokenService;
    }

    public String getToken() {
        var response = oAuth2AccessTokenService.getAccessToken(clientProperties);
        return response.getAccessToken();
    }
}
