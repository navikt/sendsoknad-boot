package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.*;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeResponse;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetsinformasjonListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetsinformasjonListeResponse;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AktivitetServiceTest {

    @Mock
    private SakOgAktivitetV1 webservice;
    @Captor
    private ArgumentCaptor<WSFinnAktivitetsinformasjonListeRequest> argument;
    @InjectMocks
    private AktivitetService aktivitetService;

    @Test
    public void skalKallePaWebService() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        String fodselnummer = "11111111111";

        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenReturn(new WSFinnAktivitetsinformasjonListeResponse());
        aktivitetService.hentAktiviteter(fodselnummer);
        verify(webservice).finnAktivitetsinformasjonListe(argument.capture());
        assertThat(argument.getValue().getPersonident()).isEqualTo(fodselnummer);
        assertThat(argument.getValue().getPeriode().getFom()).isEqualTo(LocalDate.now().minusMonths(6));
        assertThat(argument.getValue().getPeriode().getTom()).isEqualTo(LocalDate.now().plusMonths(2));
    }

    @Test
    public void skalReturnereFaktumVedUthenting() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        String fodselnummer = "11111111111";
        String aktivitetsnavn = "aktivitetsnavn";
        String id = "9999";
        String fom = "2015-02-15";
        String tom = "2015-02-28";
        String type = "arbeidspraksiss";
        String arrangoer = "Oslo kommune";

        WSPeriode periode = new WSPeriode().withFom(new LocalDate(fom)).withTom(new LocalDate(tom));
        WSAktivitetstyper aktivitetstype = new WSAktivitetstyper().withValue(type);
        WSFinnAktivitetsinformasjonListeResponse response = new WSFinnAktivitetsinformasjonListeResponse();
        response.withAktivitetListe(new WSAktivitet().withAktivitetsnavn(aktivitetsnavn).withAktivitetId(id).withPeriode(periode).withErStoenadsberettigetAktivitet(true).withAktivitetstype(aktivitetstype).withArrangoer(arrangoer));

        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenReturn(response);

        List<Faktum> fakta = aktivitetService.hentAktiviteter(fodselnummer);

        assertThat(fakta).hasSize(1);
        Faktum faktum = fakta.get(0);
        assertThat(faktum.getKey()).isEqualTo("aktivitet");
        assertThat(faktum.getProperties()).containsEntry("id", id);
        assertThat(faktum.getProperties()).containsEntry("navn", aktivitetsnavn);
        assertThat(faktum.getProperties()).containsEntry("fom", fom);
        assertThat(faktum.getProperties()).containsEntry("tom", tom);
    }

    @Test
    public void skalFiltrereBortAktiviteterSomIkkeErStonadsberettiget() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        String fodselnummer = "11111111111";
        String aktivitetsnavn = "aktivitetsnavn";
        String id = "9999";
        String fom = "2015-02-15";
        String tom = "2015-02-28";
        String type = "arbeidspraksiss";
        String arrangoer = "Oslo kommune";

        WSPeriode periode = new WSPeriode().withFom(new LocalDate(fom)).withTom(new LocalDate(tom));
        WSAktivitetstyper aktivitetstype = new WSAktivitetstyper().withValue(type);
        WSFinnAktivitetsinformasjonListeResponse response = new WSFinnAktivitetsinformasjonListeResponse();
        response.withAktivitetListe(new WSAktivitet().withAktivitetsnavn(aktivitetsnavn).withAktivitetId(id).withPeriode(periode).withErStoenadsberettigetAktivitet(true).withAktivitetstype(aktivitetstype).withArrangoer(arrangoer),
                new WSAktivitet().withAktivitetsnavn(aktivitetsnavn).withAktivitetId("8888").withPeriode(periode).withErStoenadsberettigetAktivitet(false).withAktivitetstype(aktivitetstype).withArrangoer(arrangoer));

        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenReturn(response);
        List<Faktum> fakta = aktivitetService.hentAktiviteter(fodselnummer);
        assertThat(fakta).hasSize(1);
        assertThat(fakta.get(0).getProperties().get("id")).isEqualToIgnoringCase("9999");
    }

    @Test
    public void skalReturnereFaktumUtenTom() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        String fom = "2015-02-15";
        String type = "arbeidspraksiss";
        String arrangoer = "Oslo kommune";

        WSPeriode periode = new WSPeriode().withFom(new LocalDate(fom));
        WSAktivitetstyper aktivitetstype = new WSAktivitetstyper().withValue(type);
        WSFinnAktivitetsinformasjonListeResponse response = new WSFinnAktivitetsinformasjonListeResponse();
        response.withAktivitetListe(new WSAktivitet().withPeriode(periode).withErStoenadsberettigetAktivitet(true).withAktivitetstype(aktivitetstype).withArrangoer(arrangoer));

        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenReturn(response);

        List<Faktum> fakta = aktivitetService.hentAktiviteter("11111111111");

        Faktum faktum = fakta.get(0);
        assertThat(faktum.getProperties()).containsEntry("fom", fom);
        assertThat(faktum.getProperties()).containsEntry("tom", "");
    }

    @Test
    public void skalReturnereFaktumUtenNoenPeriodedatoer() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        String type = "arbeidspraksiss";
        String arrangoer = "Oslo kommune";

        WSAktivitetstyper aktivitetstype = new WSAktivitetstyper().withValue(type);
        WSFinnAktivitetsinformasjonListeResponse response = new WSFinnAktivitetsinformasjonListeResponse();
        response.withAktivitetListe(new WSAktivitet().withPeriode(new WSPeriode()).withErStoenadsberettigetAktivitet(true).withAktivitetstype(aktivitetstype).withArrangoer(arrangoer));

        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenReturn(response);

        List<Faktum> fakta = aktivitetService.hentAktiviteter("11111111111");

        Faktum faktum = fakta.get(0);
        assertThat(faktum.getProperties()).containsEntry("fom", "");
    }

    @Test
    public void skalGodtaNullListe() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {

        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenReturn(null);

        List<Faktum> fakta = aktivitetService.hentAktiviteter("11111111111");
        assertThat(fakta).isEmpty();
    }

    @Test(expected = RuntimeException.class)
    public void skalKasteRuntimeExceptionVedWsFeil() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class)))
                .thenThrow(new FinnAktivitetsinformasjonListeSikkerhetsbegrensning());

        aktivitetService.hentAktiviteter("");
    }

    @Test
    public void skalGodtaPersonIkkeFunnet() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class)))
                .thenThrow(new FinnAktivitetsinformasjonListePersonIkkeFunnet("person ikke funnet"));

        List<Faktum> faktums = aktivitetService.hentAktiviteter("");

        assertThat(faktums).isEmpty();
    }

    @Test
    public void skalReturnereAktiveVedtak() throws FinnAktivitetOgVedtakDagligReiseListePersonIkkeFunnet, FinnAktivitetOgVedtakDagligReiseListeSikkerhetsbegrensning {
        WSFinnAktivitetOgVedtakDagligReiseListeResponse response = new WSFinnAktivitetOgVedtakDagligReiseListeResponse();
        response.withAktivitetOgVedtakListe(
                lagAktivitetOgVedtak("100", "navn på aktivitet",
                        lagVedtak(new LocalDate(2015, 1, 1), new LocalDate(2015, 3, 31), "1000", 100, true, 555.0),
                        lagVedtak(new LocalDate(2015, 4, 1), new LocalDate(2015, 5, 31), "1001", 101, true, 556.0)
                ),
                lagAktivitetOgVedtak("101", "navn på aktivitet2",
                        lagVedtak(new LocalDate(2015, 1, 1), new LocalDate(2015, 3, 31), "1000", null, false, 555.0)
                ));
        when(webservice.finnAktivitetOgVedtakDagligReiseListe(any(WSFinnAktivitetOgVedtakDagligReiseListeRequest.class))).thenReturn(response);

        List<Faktum> faktums = aktivitetService.hentVedtak("11111111111");
        ArgumentCaptor<WSFinnAktivitetOgVedtakDagligReiseListeRequest> captor = ArgumentCaptor.forClass(WSFinnAktivitetOgVedtakDagligReiseListeRequest.class);
        verify(webservice).finnAktivitetOgVedtakDagligReiseListe(captor.capture());
        assertThat(captor.getValue().getPersonident()).isEqualTo("11111111111");

        assertThat(faktums).contains(
                new Faktum()
                        .medKey("vedtak")
                        .medProperty("tema", "TSO")
                        .medProperty("aktivitetId", "100")
                        .medProperty("aktivitetNavn", "navn på aktivitet")
                        .medProperty("aktivitetFom", "2015-01-01")
                        .medProperty("aktivitetTom", "2015-12-31")
                        .medProperty("erStoenadsberettiget", "true")
                        .medProperty("fom", "2015-01-01")
                        .medProperty("tom", "2015-03-31")
                        .medProperty("trengerParkering", "true")
                        .medProperty("forventetDagligParkeringsutgift", "100")
                        .medProperty("dagsats", "555.0")
                        .medProperty("id", "1000")
        );

        assertThat(faktums).contains(
                new Faktum()
                        .medKey("vedtak")
                        .medProperty("tema", "TSO")
                        .medProperty("aktivitetId", "100")
                        .medProperty("aktivitetNavn", "navn på aktivitet")
                        .medProperty("aktivitetFom", "2015-01-01")
                        .medProperty("aktivitetTom", "2015-12-31")
                        .medProperty("erStoenadsberettiget", "true")
                        .medProperty("fom", "2015-04-01")
                        .medProperty("tom", "2015-05-31")
                        .medProperty("trengerParkering", "true")
                        .medProperty("forventetDagligParkeringsutgift", "101")
                        .medProperty("dagsats", "556.0")
                        .medProperty("id", "1001")
        );
        assertThat(faktums).contains(
                new Faktum()
                        .medKey("vedtak")
                        .medProperty("tema", "TSO")
                        .medProperty("aktivitetId", "101")
                        .medProperty("aktivitetNavn", "navn på aktivitet2")
                        .medProperty("aktivitetFom", "2015-01-01")
                        .medProperty("aktivitetTom", "2015-12-31")
                        .medProperty("erStoenadsberettiget", "true")
                        .medProperty("fom", "2015-01-01")
                        .medProperty("tom", "2015-03-31")
                        .medProperty("trengerParkering", "false")
                        .medProperty("forventetDagligParkeringsutgift", "")
                        .medProperty("dagsats", "555.0")
                        .medProperty("id", "1000")
        );
    }


    private static WSVedtaksinformasjon lagVedtak(LocalDate fom, LocalDate tom, String id, Integer forventetParkUtgift,
                                                 boolean trengerParkering, double dagsats, WSBetalingsplan... betalingsplans) {
        return new WSVedtaksinformasjon()
                .withPeriode(new WSPeriode().withFom(fom).withTom(tom))
                .withVedtakId(id)
                .withForventetDagligParkeringsutgift(forventetParkUtgift)
                .withTrengerParkering(trengerParkering)
                .withDagsats(dagsats)
                .withBetalingsplan(betalingsplans);
    }

    private static WSAktivitetOgVedtak lagAktivitetOgVedtak(String aktivitetId, String aktivitetNavn, WSVedtaksinformasjon... vedtak) {
        return new WSAktivitetOgVedtak()
                .withPeriode(new WSPeriode().withFom(new LocalDate(2015, 1, 1)).withTom(new LocalDate(2015, 12, 31)))
                .withAktivitetId(aktivitetId)
                .withAktivitetsnavn(aktivitetNavn)
                .withErStoenadsberettigetAktivitet(true)
                .withSaksinformasjon(new WSSaksinformasjon().withSaksnummerArena("saksnummerarena")
                        .withSakstype(new WSSakstyper().withValue("TSO"))
                        .withVedtaksinformasjon(vedtak));
    }
}
