package no.nav.sbl.dialogarena.common.cxf;

import no.nav.sbl.dialogarena.tokensupport.AzureAdTokenService;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;

public class AzureAdProxyAuthorizationHeaderSetterOutInterceptor extends AbstractPhaseInterceptor<Message> {
    private final AzureAdTokenService azureAdTokenService;
    
    private static final Logger logger = getLogger(AzureAdProxyAuthorizationHeaderSetterOutInterceptor.class);

    public AzureAdProxyAuthorizationHeaderSetterOutInterceptor(AzureAdTokenService azureAdTokenService) {
        super(Phase.POST_LOGICAL);
        this.azureAdTokenService = azureAdTokenService;
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        logger.debug("Executing AzureAdProxyAuthorizationHeaderSetterOutInterceptor");
        var headers = (Map<String, List>) message.get(Message.PROTOCOL_HEADERS);
        logger.debug("Setting  PROXY_AUTHORIZATION header: " + azureAdTokenService.getToken());
        headers.put(HttpHeaders.PROXY_AUTHORIZATION, List.of("Bearer " + azureAdTokenService.getToken()));
    }
}
