package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.aktivitetbetalingsplan.AktivitetBetalingsplanBolk;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetOgVedtakDagligReiseListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetOgVedtakDagligReiseListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeResponse;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AktivitetBetalingsplanBolkTest {

    @Mock
    SakOgAktivitetV1 webservice;
    @InjectMocks
    private AktivitetBetalingsplanBolk aktivitetService;

    @Test
    public void skalReturnererBetalingsplaner() throws FinnAktivitetOgVedtakDagligReiseListePersonIkkeFunnet, FinnAktivitetOgVedtakDagligReiseListeSikkerhetsbegrensning {
        WSFinnAktivitetOgVedtakDagligReiseListeResponse response = new WSFinnAktivitetOgVedtakDagligReiseListeResponse();
        response.withAktivitetOgVedtakListe(
                lagAktivitetOgVedtak("100", "navn på aktivitet",
                        lagVedtak(new LocalDate(2015, 1, 1), new LocalDate(2015, 3, 31), "1000", 100, true, 555.0,
                                lagBetalingsplan("321123", new LocalDate(2015, 1, 1), new LocalDate(2015, 1, 7), "1232312323").withBeloep(444.0),
                                lagBetalingsplan("321124", new LocalDate(2015, 1, 7), new LocalDate(2015, 1, 14), null).withBeloep(333.3),
                                lagBetalingsplan("321125", new LocalDate(2015, 1, 14), new LocalDate(2015, 1, 21), null)
                        ),
                        lagVedtak(new LocalDate(2015, 4, 1), new LocalDate(2015, 5, 31), "1001", 101, true, 556.0,
                                lagBetalingsplan("321126", new LocalDate(2015, 1, 14), new LocalDate(2015, 1, 21), null)
                        )
                ),
                lagAktivitetOgVedtak("101", "navn på aktivitet2",
                        lagVedtak(new LocalDate(2015, 1, 1), new LocalDate(2015, 3, 31), "1000", null, false, 555.0)
                ));
        when(webservice.finnAktivitetOgVedtakDagligReiseListe(any(WSFinnAktivitetOgVedtakDagligReiseListeRequest.class))).thenReturn(response);

        List<Faktum> faktums = aktivitetService.hentBetalingsplanerForVedtak(10L, "12312312345", "100", "1000");
        assertThat(faktums).hasSize(3);
        assertThat(faktums).contains(new Faktum()
                        .medSoknadId(10L)
                        .medKey("vedtak.betalingsplan")
                        .medProperty("uniqueKey", "id")
                        .medProperty("id", "321123")
                        .medProperty("fom", "2015-01-01")
                        .medProperty("tom", "2015-01-07")
                        .medProperty("alleredeSokt", "true")
                        .medProperty("refunderbartBeloep", "444.0")
                        .medProperty("sokerForPeriode", "false")
        );
        assertThat(faktums).contains(new Faktum()
                        .medSoknadId(10L)
                        .medKey("vedtak.betalingsplan")
                        .medProperty("uniqueKey", "id")
                        .medProperty("id", "321124")
                        .medProperty("fom", "2015-01-07")
                        .medProperty("tom", "2015-01-14")
                        .medProperty("alleredeSokt", "false")
                        .medProperty("refunderbartBeloep", "333.3")
        );
        assertThat(faktums).contains(new Faktum()
                        .medSoknadId(10L)
                        .medKey("vedtak.betalingsplan")
                        .medProperty("uniqueKey", "id")
                        .medProperty("id", "321125")
                        .medProperty("fom", "2015-01-14")
                        .medProperty("tom", "2015-01-21")
                        .medProperty("alleredeSokt", "false")
                        .medProperty("refunderbartBeloep", "0.0")
        );
    }

    private static WSBetalingsplan lagBetalingsplan(String betPlanId, LocalDate fom, LocalDate tom, String journalpostId) {
        return new WSBetalingsplan().withJournalpostId(journalpostId).withBetalingsplanId(betPlanId).withUtgiftsperiode(new WSPeriode().withFom(fom).withTom(tom));
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
