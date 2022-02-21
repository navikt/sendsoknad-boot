package no.nav.sbl.dialogarena.common.cxf;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class AttachApiKeyOutInterceptor extends AbstractPhaseInterceptor<Message>{

    private static final String API_KEY_HEADER = "x-nav-apiKey";
    private String apiKeyValue;
    
    public AttachApiKeyOutInterceptor(String apiKey) {
        super(Phase.POST_PROTOCOL);
        this.apiKeyValue = apiKey;
    }
    
    private static String encodeAsBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }
    
    @Override
    public void handleMessage(Message message) throws Fault {
       
         Map<String,List> headers =(Map<String, List>) message.get(Message.PROTOCOL_HEADERS);
         
         headers.put(API_KEY_HEADER, Arrays.asList(apiKeyValue));
         
         String userName = System.getProperty("systemuser.sendsoknad.username");
         String password = System.getProperty("systemuser.sendsoknad.password");
         headers.put("Authorization", List.of("Basic "+ encodeAsBase64(userName+":"+password)));
    }
    
    

}
