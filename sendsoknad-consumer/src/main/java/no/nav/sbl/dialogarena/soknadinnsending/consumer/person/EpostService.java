package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;

import no.nav.modig.common.MDCOperations;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class EpostService {
    private static final Logger logger = getLogger(EpostService.class);

    private final RestTemplate restTemplate;

    @Autowired
    public EpostService(
            @Qualifier("digdirKrrProxyRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Cacheable("dkifCache")
    public DigitalKontaktinfo hentDigitalKontaktinfo(String ident) {
        try {
            logger.info("Start kall til Dkif-krr-proxy");
            RequestEntity<Void> requestEntity = RequestEntity
                    .get("/rest/v1/person")
                    .header("Nav-Call-Id", resolveCallId())
                    .header("Nav-Personident", ident)
                    .build();
            var responseEntity = restTemplate.exchange(requestEntity, DigitalKontaktinfo.class);
            return toDigitalKontaktinfo(responseEntity.getBody());
        } catch (HttpClientErrorException exception) {
            logger.warn("En klientfeil oppsto ved henting av digital kontaktinformasjon fra digdir-krr-proxy.", exception);
            return new DigitalKontaktinfo("", "");
        } catch (Exception exception) {
            logger.warn("En ukjent feil oppsto ved henting av digital kontaktinformasjon fra digdir-krr-proxy.", exception);
            return new DigitalKontaktinfo("", "");
        } finally {
            logger.info("End kall til Dkif-krr-proxy");
        }
    }

    private String resolveCallId() {
        String callIdFromMdc = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        return Objects.requireNonNullElseGet(callIdFromMdc, () -> UUID.randomUUID().toString());
    }

    private DigitalKontaktinfo toDigitalKontaktinfo(DigitalKontaktinfo original) {
        return new DigitalKontaktinfo(
                Objects.requireNonNullElse(original.epostadresse(), ""),
                Objects.requireNonNullElse(original.mobiltelefonnummer(), ""));
    }

    public record DigitalKontaktinfo(String epostadresse, String mobiltelefonnummer) {
    }

}
