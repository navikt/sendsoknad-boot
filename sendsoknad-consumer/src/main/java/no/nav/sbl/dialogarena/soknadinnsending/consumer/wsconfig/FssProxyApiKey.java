package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import no.nav.modig.common.MDCOperations;


public class FssProxyApiKey {
    protected static final Logger log = LoggerFactory.getLogger(MDCOperations.class.getName());

    
    public static String value;

    @Value("${api-key.soknad-fss-proxy}")
    public void initialize(String apiKey) {
        log.info("api key is " + apiKey);
        FssProxyApiKey.value=apiKey;
    }
}
