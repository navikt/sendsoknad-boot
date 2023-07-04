package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;

import no.nav.modig.common.MDCOperations;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSMobiltelefonnummer;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class EpostService {
    private static final Logger logger = getLogger(EpostService.class);

    private final RestTemplate restTemplate;
    private final DigitalKontaktinformasjonV1 dkif;

    @Autowired
    public EpostService(
            @Qualifier("dkifService") DigitalKontaktinformasjonV1 dkif,
            @Qualifier("digdirKrrProxyRestTemplate") RestTemplate restTemplate) {
        this.dkif = dkif;
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
            logger.warn("En klientfeil oppsto ved henting av digital kontaktinformasjon fra digdir-krr-proxy." +
                            " Henter fra dkif isteden.\nWWW-Authenticate={}",
                    exception.getResponseHeaders().get("WWW-Authenticate"),
                    exception);
            return hentInfoFraLegacyDKIF(ident);
        } catch (Exception exception) {
            logger.warn("En ukjent feil oppsto ved henting av digital kontaktinformasjon fra digdir-krr-proxy." +
                    " Henter fra dkif isteden", exception);
            return hentInfoFraLegacyDKIF(ident);
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

    @Deprecated(forRemoval = true)
    private DigitalKontaktinfo hentInfoFraLegacyDKIF(String ident) {
        try {
            var request = new WSHentDigitalKontaktinformasjonRequest().withPersonident(ident);
            var response = dkif.hentDigitalKontaktinformasjon(request);
            if (response != null && response.getDigitalKontaktinformasjon() != null) {
                return toDigitalKontaktinfo(response.getDigitalKontaktinformasjon());
            }
        } catch (HentDigitalKontaktinformasjonSikkerhetsbegrensing | HentDigitalKontaktinformasjonPersonIkkeFunnet e) {
            logger.warn("Person ikke tilgjengelig i dkif: {}", e.getMessage());
        } catch (HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet e) {
            logger.info("Kunne ikke hente kontaktinformasjon fra dkif: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Hent info fra DKIF feiler med {}", e.getMessage(), e);
        }

        return new DigitalKontaktinfo("", "");
    }

    private DigitalKontaktinfo toDigitalKontaktinfo(WSKontaktinformasjon original) {
        String epostadresse = Optional.ofNullable(original.getEpostadresse())
                .map(WSEpostadresse::getValue)
                .orElse("");
        String mobiltelefonnummer = Optional.ofNullable(original.getMobiltelefonnummer())
                .map(WSMobiltelefonnummer::getValue)
                .orElse("");
        return new DigitalKontaktinfo(epostadresse, mobiltelefonnummer);
    }

}
