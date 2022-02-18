package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class WSConfig {
    
    @Value("${api-key.soknad-fss-proxy}")
    protected String apiKey;
    
    @Value("${systemuser.sendsoknad.username}") 
    protected String systemUser;
    @Value("${systemuser.sendsoknad.password}") 
    protected String systemPassword;


}
