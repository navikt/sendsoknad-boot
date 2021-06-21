package no.nav.modig.testcertificates;

import java.util.Properties;

import no.nav.modig.core.test.PropertySetter;

public class KeyStore extends PropertySetter {

    public KeyStore(String filePath, String password) {
        super(createKeyStoreProperties(filePath, password));
    }

    private static Properties createKeyStoreProperties(String filePath, String password) {
        Properties props = new Properties();
        props.setProperty("no.nav.modig.security.appcert.keystore", filePath);
        props.setProperty("no.nav.modig.security.appcert.password", password);
        return props;
    }
}