package no.nav.modig.testcertificates;

import no.nav.modig.core.test.PropertySetter;

import java.util.Properties;

public class TrustStore extends PropertySetter {

    public TrustStore(String truststoreFilePath, String truststorePassword) {
        super(createTrustStoreProperties(truststoreFilePath, truststorePassword));
    }

    private static Properties createTrustStoreProperties(String truststoreFilePath, String truststorePassword) {
        Properties props = new Properties();
        props.setProperty("javax.net.ssl.trustStore", truststoreFilePath);
        props.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
        return props;
    }
}