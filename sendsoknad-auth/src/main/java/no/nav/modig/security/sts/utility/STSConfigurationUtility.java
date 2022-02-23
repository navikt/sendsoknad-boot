package no.nav.modig.security.sts.utility;

import no.nav.modig.common.SpringContextAccessor;
import no.nav.modig.security.sts.client.NavStsRestClient;
import no.nav.sbl.dialogarena.common.cxf.AttachSamlHeaderOutInterceptor;
import no.nav.sbl.dialogarena.tokensupport.TokenUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import java.util.function.Supplier;

/**
 * A collection of configuration methods to configure an CXF WS-client
 * to use STS to retrieve SAML tokens for end user and system user.
 */
public class STSConfigurationUtility {
    /**
     * Configures endpoint to get SAML token for the end user from STS in exchange for OpenAM token.
     * The SAML token will be added as a SupportingToken to the WS-Security headers.
     * <p/>
     * 1. Binds a WS-SecurityPolicy to the endpoint/client.
     * The policy requires a SupportingToken of type IssuedToken.
     * <p/>
     * 2. Configures the location and credentials of the STS.
     *
     * @param client CXF client
     */
    public static void configureStsForExternalSSO(Client client) {
        Supplier<String> samlXmlSupplier = () -> {
            var stsRestClient = SpringContextAccessor.getBean(NavStsRestClient.class);
            var encodedTokenX = TokenUtils.getTokenAsString(TokenUtils.ISSUER_TOKENX);
            return stsRestClient.exchangeForSaml(encodedTokenX).decodedToken();
        };

        configureSts(client, samlXmlSupplier);
    }

    /**
     * Configures endpoint to get SAML token for the system user from STS.
     * The SAML token will be added as a SupportingToken to the WS-Security headers.
     * <p/>
     * 1. Binds a WS-SecurityPolicy to the endpoint/client.
     * The policy requires a SupportingToken of type IssuedToken.
     * <p/>
     * 2. Configures the location and credentials of the STS.
     *
     * @param client CXF client
     */
    public static void configureStsForSystemUser(Client client) {
        Supplier<String> samlXmlSupplier = () -> {
            var stsRestClient = SpringContextAccessor.getBean(NavStsRestClient.class);
            return stsRestClient.getSystemSaml().decodedToken();
        };

        configureSts(client, samlXmlSupplier);
    }

    private static void configureSts(Client client, Supplier<String> samlXmlSupplier) {
        var interceptor = new AttachSamlHeaderOutInterceptor(samlXmlSupplier);
        client.getOutInterceptors().add(interceptor);

        new WSAddressingFeature().initialize(client, client.getBus());
    }
}
