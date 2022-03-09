package no.nav.sbl.dialogarena.common.cxf;

import no.nav.sbl.dialogarena.tokensupport.AzureAdTokenService;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;

public class AzureAdProxyAuthorizationHeaderSetterOutInterceptor extends AbstractPhaseInterceptor<Message> {
    private final AzureAdTokenService azureAdTokenService;

    public AzureAdProxyAuthorizationHeaderSetterOutInterceptor(AzureAdTokenService azureAdTokenService) {
        super(Phase.POST_LOGICAL);
        this.azureAdTokenService = azureAdTokenService;
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        var headers = (Map<String, List>) message.get(Message.PROTOCOL_HEADERS);
        headers.put(HttpHeaders.PROXY_AUTHORIZATION, List.of("Bearer " + azureAdTokenService.getToken()));
    }
}
