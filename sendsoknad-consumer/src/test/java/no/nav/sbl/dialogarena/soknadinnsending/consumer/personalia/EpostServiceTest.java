package no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.EpostService;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSMobiltelefonnummer;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonRequest;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;


@RunWith(value = MockitoJUnitRunner.class)
public class EpostServiceTest {

    @InjectMocks
    private EpostService epostService;

    @Mock
    private DigitalKontaktinformasjonV1 dkif;

    @Mock
    private RestTemplate restTemplate;


    @Test
    public void sendsCorrectRequestToDigdirKrrProxy() {
        String fnr = "12345612345";

        when(restTemplate.exchange(any(RequestEntity.class), eq(EpostService.DigitalKontaktinfo.class)))
                .thenReturn(new ResponseEntity<>(new EpostService.DigitalKontaktinfo("", ""), HttpStatus.OK));

        epostService.hentDigitalKontaktinfo(fnr);

        var requestEntityCaptor = ArgumentCaptor.forClass(RequestEntity.UriTemplateRequestEntity.class);
        verify(restTemplate).exchange(requestEntityCaptor.capture(), eq(EpostService.DigitalKontaktinfo.class));
        assertThat(requestEntityCaptor.getValue().getUriTemplate()).isEqualTo("/rest/v1/person");
        assertThat(requestEntityCaptor.getValue().getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(requestEntityCaptor.getValue().getHeaders()).containsEntry("Nav-Personident", List.of(fnr));
        assertThat(requestEntityCaptor.getValue().getHeaders()).containsKey("Nav-Call-Id");
        assertThat(requestEntityCaptor.getValue().getBody()).isNull();
    }

    @Test
    public void returnsResponseFromDigdirKrrProxyOnSuccess() {
        String fnr = "12345612345";

        var digdirKrrProxyResponse = new EpostService.DigitalKontaktinfo("test@test.no", "12345678");
        when(restTemplate.exchange(any(RequestEntity.class), eq(EpostService.DigitalKontaktinfo.class)))
                .thenReturn(new ResponseEntity<>(digdirKrrProxyResponse, HttpStatus.OK));

        EpostService.DigitalKontaktinfo result = epostService.hentDigitalKontaktinfo(fnr);

        assertThat(result.epostadresse()).isEqualTo("test@test.no");
        assertThat(result.mobiltelefonnummer()).isEqualTo("12345678");
    }

    @Test
    public void returnsEmptyResponseFromDigdirKrrProxyWhenNoEpostOrMobil() {
        String fnr = "12345612345";

        var digdirKrrProxyResponse = new EpostService.DigitalKontaktinfo(null, null);
        when(restTemplate.exchange(any(RequestEntity.class), eq(EpostService.DigitalKontaktinfo.class)))
                .thenReturn(new ResponseEntity<>(digdirKrrProxyResponse, HttpStatus.OK));

        EpostService.DigitalKontaktinfo result = epostService.hentDigitalKontaktinfo(fnr);

        assertThat(result.epostadresse()).isEmpty();
        assertThat(result.mobiltelefonnummer()).isEmpty();
    }

    @Test
    public void returnsResponseFromDkifOnFailure() throws Exception {
        String fnr = "12345612345";

        when(restTemplate.exchange(any(RequestEntity.class), eq(EpostService.DigitalKontaktinfo.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", new HttpHeaders(), null, null));

        var dkifResponse = new WSHentDigitalKontaktinformasjonResponse()
                .withDigitalKontaktinformasjon(new WSKontaktinformasjon()
                        .withEpostadresse(new WSEpostadresse().withValue("test@test.no"))
                        .withMobiltelefonnummer(new WSMobiltelefonnummer().withValue("12345678")));
        when(dkif.hentDigitalKontaktinformasjon(any(WSHentDigitalKontaktinformasjonRequest.class)))
                .thenReturn(dkifResponse);

        EpostService.DigitalKontaktinfo result = epostService.hentDigitalKontaktinfo(fnr);

        assertThat(result.epostadresse()).isEqualTo("test@test.no");
        assertThat(result.mobiltelefonnummer()).isEqualTo("12345678");
    }

    @Test
    public void returnsEmptyDigitalKontaktinfoOnFailure() throws Exception {
        String fnr = "12345612345";
        when(restTemplate.exchange(any(RequestEntity.class), eq(EpostService.DigitalKontaktinfo.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", new HttpHeaders(), null, null));

        when(dkif.hentDigitalKontaktinformasjon(any(WSHentDigitalKontaktinformasjonRequest.class)))
                .thenThrow(new HentDigitalKontaktinformasjonSikkerhetsbegrensing());

        EpostService.DigitalKontaktinfo result = epostService.hentDigitalKontaktinfo(fnr);
        assertThat(result.epostadresse()).isEmpty();
        assertThat(result.mobiltelefonnummer()).isEmpty();
    }
}
