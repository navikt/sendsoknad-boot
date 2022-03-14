package no.nav.sbl.dialogarena.common.cxf;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class HttpRequestHeaderSetterOutInterceptor extends AbstractPhaseInterceptor<Message> {
    private final Supplier<Map<String, List<String>>> httpHeaderSupplier;

    public HttpRequestHeaderSetterOutInterceptor(Supplier<Map<String, List<String>>> httpHeaderSupplier) {
        super(Phase.POST_LOGICAL);
        this.httpHeaderSupplier = httpHeaderSupplier;
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        var headers = (Map<String, List>) message.get(Message.PROTOCOL_HEADERS);
        if (headers != null) {
            headers.putAll(httpHeaderSupplier.get());
        }
    }
}
