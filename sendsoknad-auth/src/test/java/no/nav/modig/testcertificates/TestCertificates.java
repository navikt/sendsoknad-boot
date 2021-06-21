package no.nav.modig.testcertificates;

import java.io.InputStream;

import static no.nav.modig.core.test.FileUtils.putInTempFile;

public final class TestCertificates {

    /**
     * Sett opp felles key og trust stores for testing
     */
    public static void setupKeyAndTrustStore() {
        TestCertificates.setupTemporaryKeyStore("no/nav/modig/testcertificates/keystore.jks", "devillokeystore1234");
        TestCertificates.setupTemporaryTrustStore("no/nav/modig/testcertificates/truststore.jts", "changeit");
    }

    /**
     * sett opp key store for testing
     */
    public static void setupTemporaryKeyStore(String keyStoreResourceName, String password) {
        InputStream keyStore = TestCertificates.class.getClassLoader().getResourceAsStream(keyStoreResourceName);
        setupTemporaryKeyStore(keyStore, password);
    }

    /**
     * sett opp key store for testing
     */
    public static void setupTemporaryKeyStore(InputStream keystore, String password) {
        new KeyStore(putInTempFile(keystore).getAbsolutePath(), password).setOn(System.getProperties());
    }

    /**
     * sett opp trust store for testing
     */
    public static void setupTemporaryTrustStore(String trustStoreResourceName, String password) {
        InputStream trustStore = TestCertificates.class.getClassLoader().getResourceAsStream(trustStoreResourceName);
        setupTemporaryTrustStore(trustStore, password);
    }

    /**
     * sett opp trust store for testing
     */
    public static void setupTemporaryTrustStore(InputStream trustStore, String password) {
        new TrustStore(putInTempFile(trustStore).getAbsolutePath(), password).setOn(System.getProperties());
    }

    private TestCertificates() {
    }
}