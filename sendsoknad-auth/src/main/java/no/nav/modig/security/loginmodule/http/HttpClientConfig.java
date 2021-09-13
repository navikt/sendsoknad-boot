package no.nav.modig.security.loginmodule.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import no.nav.modig.security.loginmodule.http.ssl.AdditionalKeyStoresTrustManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpClientConfig {

    private URI endpointURL;
    private boolean ssl;
    private KeyStore trustStore;

    /**
     * Creates a HttpClient. Uses default truststore if endpoint uses https.
     * 
     * @param endpointURL
     *            the endpointURL
     */
    public HttpClientConfig(URI endpointURL) {
        this.endpointURL = endpointURL;
    }

    /**
     * Creates a HttpClient using the supplied truststore file.
     * 
     * @param endpointURL
     *            the endpointURL
     * @param trustStoreFile
     *            path to truststore for the ssl connection, default truststore will be used if null
     * @param trustStorePassword
     *            the truststore password
     */
    public HttpClientConfig(URI endpointURL, File trustStoreFile, String trustStorePassword) {
        this.endpointURL = endpointURL;
        String scheme = endpointURL.getScheme();
        this.ssl = scheme != null && scheme.equalsIgnoreCase("https");
        this.trustStore = trustStoreFile != null && trustStorePassword != null ?
                loadTrustStore(KeyStore.getDefaultType(), trustStoreFile, trustStorePassword, null) : null;
    }

    public String getEndpoint() {
        return endpointURL.toString();
    }

    public HttpClient createHttpClient() throws GeneralSecurityException {
        return useSSL() ? createSecureHttpClient() : new DefaultHttpClient();
    }

    public boolean useSSL() {
        return this.ssl;
    }

    protected HttpClient createSecureHttpClient() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[] { new AdditionalKeyStoresTrustManager(trustStore) }, null);

        SSLSocketFactory sslSocketFactory = new SSLSocketFactory(ctx);// , SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        HttpClient httpClient = new DefaultHttpClient();
        ClientConnectionManager conManager = httpClient.getConnectionManager();
        SchemeRegistry schemeRegistry = conManager.getSchemeRegistry();
        schemeRegistry.register(new Scheme("https", 443, sslSocketFactory));
        return httpClient;
    }

    protected KeyStore getTrustStore() {
        return trustStore;
    }

    protected void setTrustStore(KeyStore trustStore) {
        this.trustStore = trustStore;
    }

    /**
     * gets a keystore instance
     * 
     * @param type
     * @param path
     * @param password
     * @param provider
     * @return
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     */
    private KeyStore loadTrustStore(String type, File path, String password, Provider provider) {
        FileInputStream stream = null;
        KeyStore keyStore = null;
        try {
            keyStore = useProvider(provider, type) ? KeyStore.getInstance(type, provider) : KeyStore.getInstance(type);
            stream = FileUtils.openInputStream(path);
            keyStore.load(stream, password.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("Could not load truststore.", e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return keyStore;
    }

    /**
     * checks if provider should be used
     * 
     * @param provider
     * @param type
     * @return
     */
    private static boolean useProvider(Provider provider, String type) {
        return provider != null && type.equalsIgnoreCase("PKCS12");
    }
    
    @Override
    public String toString(){
    	return this.getClass() + "[endpoint="+getEndpoint()+"]";
    }
}
