package no.nav.modig.security.loginmodule.userinfo.openam;

import no.nav.modig.security.loginmodule.http.HttpClientConfig;
import no.nav.modig.security.loginmodule.userinfo.AbstractUserInfoService;
import no.nav.modig.security.loginmodule.userinfo.UserInfo;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static no.nav.modig.security.loginmodule.userinfo.openam.OpenAMAttributes.PARAMETER_SECURITY_LEVEL;
import static no.nav.modig.security.loginmodule.userinfo.openam.OpenAMAttributes.PARAMETER_UID;

public class OpenAMUserInfoService extends AbstractUserInfoService {
    private static final Logger log = LoggerFactory.getLogger(OpenAMUserInfoService.class);

    private static final String OPENAM_GENERAL_ERROR = "Could not get user attributes from OpenAM. ";
    private static final String BASE_PATH = "/identity/json/attributes";
    private HttpClientConfig httpClientConfig;
    protected HttpClient client;

    public OpenAMUserInfoService(URI endpointURL) {
        this(endpointURL, null, null);
    }

    public OpenAMUserInfoService(URI endpointURL, File trustStoreFile, String trustStorePassword) {
        this(new HttpClientConfig(endpointURL, trustStoreFile, trustStorePassword));
    }

    public OpenAMUserInfoService(HttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
        try {
            client = httpClientConfig.createHttpClient();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpClientConfig getHttpClientConfig() {
        return httpClientConfig;
    }

    @Override
    public UserInfo getUserInfo(String subjectId) {
        String response = invokeRestClient(subjectId);
        return createUserInfo(response);
    }

    protected String invokeRestClient(String subjectId) {
        log.debug("Invoking OpenAM REST interface.");
        String url = httpClientConfig.getEndpoint() + BASE_PATH + format("?subjectid=%s&attributenames=%s&attributenames=%s", subjectId, PARAMETER_UID, PARAMETER_SECURITY_LEVEL);
        HttpGet request = new HttpGet(url);
        HttpResponse response;
        try {
            response = client.execute(request);
            int status = response.getStatusLine().getStatusCode();
            String phrase = response.getStatusLine().getReasonPhrase();
            String payload = getPayloadAsString(response);
            if (status < 399) {
                log.debug("Received response: " + payload);
                return payload;
            } else {
                String message = OPENAM_GENERAL_ERROR + "HTTP status: " + status + " " + phrase + ".";
                if (status == 401) {
                    message += " Response:" + payload;
                }
                log.debug(message);
                throw new OpenAMException(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            request.releaseConnection();
        }
    }

    private String getPayloadAsString(HttpResponse response) {
        try {
            InputStream inputStream = response.getEntity().getContent();
            return IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected UserInfo createUserInfo(String response) {
        Map<String, String> attributeMap = parseUserAttributes(response);

        if (attributeMap.containsKey(PARAMETER_UID)
                && attributeMap.containsKey(PARAMETER_SECURITY_LEVEL)) {

            return new UserInfo(attributeMap.get(PARAMETER_UID),
                    Integer.valueOf(attributeMap.get(PARAMETER_SECURITY_LEVEL)));
        } else {
            throw new OpenAMException(OPENAM_GENERAL_ERROR + "Response did not contain attributes "
                    + PARAMETER_UID + " and/or " + PARAMETER_SECURITY_LEVEL);
        }
    }

    protected Map<String, String> parseUserAttributes(String response) {
        try {
            JSONObject json = new JSONObject(response);
            Object o = json.get(OpenAMAttributes.PARAMETER_ATTRIBUTES);
            Map<String, String> attributeMap = new HashMap<String, String>();
            JSONArray array = (JSONArray) o;
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = (JSONObject) array.get(i);
                String name = obj.getString(OpenAMAttributes.PARAMETER_NAME);
                JSONArray values = (JSONArray) obj.get(OpenAMAttributes.PARAMETER_VALUES);
                String value = values.getString(0);
                attributeMap.put(name, value);
            }
            return attributeMap;
        } catch (JSONException e) {
            throw new OpenAMException("Error parsing JSON response. ", e);
        }
    }

    @Override
    public String toString() {
        return this.getClass() + "[" + httpClientConfig + "]";
    }

}
