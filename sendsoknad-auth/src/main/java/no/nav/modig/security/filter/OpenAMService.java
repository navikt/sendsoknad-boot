package no.nav.modig.security.filter;

import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

/**
 *  Service som sjekker om OpenAM token er gyldig eller ikke.
 */
public class OpenAMService {

	Logger LOG = LoggerFactory.getLogger(OpenAMService.class);
	
	private static final String BASE_PATH = "/identity/json/isTokenValid";
	private final String restUrl;
	private final HttpClient client;

    public OpenAMService() {
    	restUrl = System.getProperty("openam.restUrl");
        if (restUrl == null) {
            throw new RuntimeException("System property openam.restUrl er ikke tilgjengelig. Denne mÃ¥ vÃ¦re konfigurert opp for Ã¥ kunne kjÃ¸re pÃ¥logging med OpenAM.");
        }
        client = new SystemDefaultHttpClient();
    }

    public boolean isTokenValid(String tokenid) {
		LOG.debug("Sjekker om token er gyldig");
		String url = restUrl + BASE_PATH + format("?tokenid=%s", tokenid);
		HttpGet request = new HttpGet(url);
		try {
			HttpResponse response = client.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			switch (statusCode) {
			case SC_OK:
				return true;
			case SC_UNAUTHORIZED:
				return false;
			default:
				throw new IllegalStateException(
						"Kunne ikke verifisere om token er gyldig. Fikk statuskode '"
								+ statusCode + "'");
			}
		} catch (IOException ioe) {
			throw new IllegalStateException(
					"Kunne ikke verifisere om token er gyldig.", ioe);
		} finally {
			request.releaseConnection();
		}
    }
}
