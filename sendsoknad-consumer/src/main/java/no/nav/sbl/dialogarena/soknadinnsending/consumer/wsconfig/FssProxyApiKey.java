package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FssProxyApiKey {
    
    
    public static String value;

    @Value("${api-key.soknad-fss-proxy}")
    public void initialize(String apiKey) {
        FssProxyApiKey.value=apiKey;
    }
}
