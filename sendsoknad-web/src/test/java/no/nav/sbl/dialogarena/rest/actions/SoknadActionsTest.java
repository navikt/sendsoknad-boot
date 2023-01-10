package no.nav.sbl.dialogarena.rest.actions;

import no.nav.sbl.dialogarena.config.SoknadActionsTestConfig;
import no.nav.sbl.dialogarena.rest.meldinger.FortsettSenere;
import no.nav.sbl.dialogarena.rest.meldinger.SoknadBekreftelse;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPGjenopptakInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPOrdinaerInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPUtlandetInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadTilleggsstonader;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.service.HtmlGenerator;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SoknadActionsTestConfig.class})
public class SoknadActionsTest {

    private static final String SKJEMANUMMER_KVITTERING = "L7";
    private static final String BEHANDLINGS_ID = "123";
    private static final String SOKNADINNSENDING_ETTERSENDING_URL = "/soknadinnsending/ettersending";
    private static final String SOKNADINNSENDING_URL = "http://localhost:8282/soknadinnsending";
    private static final String SAKSOVERSIKT_URL = "/saksoversikt";

    @Autowired
    TekstHenter tekster;
    @Autowired
    SoknadService soknadService;
    @Autowired
    HtmlGenerator pdfTemplate;
    @Autowired
    SoknadActions actions;
    @Autowired
    WebSoknadConfig webSoknadConfig;

    private ServletContext context = mock(ServletContext.class);

    @Before
    public void setup() {
        System.setProperty("soknadinnsending.link.url", SOKNADINNSENDING_URL);
        System.setProperty("soknadinnsending.ettersending.path", SOKNADINNSENDING_ETTERSENDING_URL);
        System.setProperty("saksoversikt.link.url", SAKSOVERSIKT_URL);
        reset(tekster, webSoknadConfig, pdfTemplate);
        when(tekster.finnTekst(eq("sendtSoknad.sendEpost.epostSubject"), any(Object[].class), any(Locale.class))).thenReturn("Emne");
        when(context.getRealPath(anyString())).thenReturn("");
        when(webSoknadConfig.brukerNyOppsummering(anyLong())).thenReturn(false);
        when(webSoknadConfig.skalSendeMedFullSoknad(anyLong())).thenReturn(false);
    }

    @Test
    public void sendSoknadSkalLageAAPOrdinaerInformasjonPdfMedKodeverksverdier() throws Exception {
        AAPOrdinaerInformasjon aapOrdinaerInformasjon = new AAPOrdinaerInformasjon();

        when(soknadService.hentSoknad(BEHANDLINGS_ID, true, true)).thenReturn(
                soknad().medSoknadPrefix(aapOrdinaerInformasjon.getSoknadTypePrefix()).medStatus(SoknadInnsendingStatus.UNDER_ARBEID));
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn("<html></html>");

        actions.sendSoknad(BEHANDLINGS_ID, context);

        verify(pdfTemplate, times(1)).fyllHtmlMalMedInnhold(any(WebSoknad.class), eq("/skjema/kvittering"));
        verify(pdfTemplate, times(1)).fyllHtmlMalMedInnhold(any(WebSoknad.class), eq("/skjema/aap.ordinaer"));
    }

    @Test
    public void sendSoknadSkalIkkeSendeL7VedleggForAAPUtland() throws Exception {
        AAPUtlandetInformasjon aapUtlandetInformasjon = new AAPUtlandetInformasjon();

        when(soknadService.hentSoknad(BEHANDLINGS_ID, true, true)).thenReturn(
                soknad().medskjemaNummer(aapUtlandetInformasjon.getSkjemanummer().get(0))
                        .medSoknadPrefix(aapUtlandetInformasjon.getSoknadTypePrefix()).medStatus(SoknadInnsendingStatus.UNDER_ARBEID));
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn("<html></html>");

        actions.sendSoknad(BEHANDLINGS_ID, context);

        verify(pdfTemplate).fyllHtmlMalMedInnhold(any(WebSoknad.class), eq("/skjema/aap.utland"));
        verify(pdfTemplate, times(1)).fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString());
    }

    @Test
    public void sendSoknadSkalBrukeNyPdfLogikkOmDetErSattPaaConfig() throws Exception {
        AAPOrdinaerInformasjon aapOrdinaerInformasjon = new AAPOrdinaerInformasjon();

        when(soknadService.hentSoknad(BEHANDLINGS_ID, true, true)).thenReturn(soknad().medSoknadPrefix(aapOrdinaerInformasjon.getSoknadTypePrefix()).medStatus(SoknadInnsendingStatus.UNDER_ARBEID));
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyBoolean())).thenReturn("<html></html>");
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn("<html></html>");
        when(webSoknadConfig.brukerNyOppsummering(anyLong())).thenReturn(true);

        actions.sendSoknad(BEHANDLINGS_ID, context);

        verify(pdfTemplate).fyllHtmlMalMedInnhold(any(WebSoknad.class), eq("/skjema/kvittering"));
    }

    @Test
    public void sendSoknadSkalSendeMedUtvidetSoknadOmDetErSattPaaConfig() throws Exception {
        AAPOrdinaerInformasjon aapOrdinaerInformasjon = new AAPOrdinaerInformasjon();

        when(soknadService.hentSoknad(BEHANDLINGS_ID, true, true)).thenReturn(
                soknad().medSoknadPrefix(aapOrdinaerInformasjon.getSoknadTypePrefix()).medStatus(SoknadInnsendingStatus.UNDER_ARBEID));
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyBoolean())).thenReturn("<html></html>").thenReturn("<html></html>");
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn("<html></html>");
        when(webSoknadConfig.brukerNyOppsummering(anyLong())).thenReturn(true);
        when(webSoknadConfig.skalSendeMedFullSoknad(anyLong())).thenReturn(true);
        actions.sendSoknad(BEHANDLINGS_ID, context);

        verify(pdfTemplate).fyllHtmlMalMedInnhold(any(WebSoknad.class), eq("/skjema/kvittering"));
    }

    @Test
    public void sendSoknadSkalAvviseInnsendingDersomIkkeStatusUnderArbeid() throws Exception {
        AAPOrdinaerInformasjon aapOrdinaerInformasjon = new AAPOrdinaerInformasjon();

        when(soknadService.hentSoknad(BEHANDLINGS_ID, true, true)).thenReturn(
                soknad().medSoknadPrefix(aapOrdinaerInformasjon.getSoknadTypePrefix()).medStatus(SoknadInnsendingStatus.FERDIG));

        assertThrows(SendSoknadException.class, () -> actions.sendSoknad(BEHANDLINGS_ID, context));

    }

    @Test
    public void sendGjenopptakSkalLageGjenopptakPdfMedKodeverksverdier() throws Exception {
        AAPGjenopptakInformasjon aapGjenopptakInformasjon = new AAPGjenopptakInformasjon();

        when(soknadService.hentSoknad(BEHANDLINGS_ID, true, true)).thenReturn(soknad().medSoknadPrefix(aapGjenopptakInformasjon.getSoknadTypePrefix()).medStatus(SoknadInnsendingStatus.UNDER_ARBEID));
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn("<html></html>");

        actions.sendSoknad(BEHANDLINGS_ID, context);

        verify(pdfTemplate).fyllHtmlMalMedInnhold(any(WebSoknad.class), eq("/skjema/aap.gjenopptak"));
    }

    @Test
    public void sendEttersendingSkalLageEttersendingDummyPdf() throws Exception {
        when(soknadService.hentSoknad(BEHANDLINGS_ID, true, true)).thenReturn(soknadMedVedlegg().medDelstegStatus(DelstegStatus.ETTERSENDING_OPPRETTET).medStatus(SoknadInnsendingStatus.UNDER_ARBEID));
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn("<html></html>");

        actions.sendSoknad(BEHANDLINGS_ID, context);

        verify(pdfTemplate).fyllHtmlMalMedInnhold(any(WebSoknad.class), eq("skjema/ettersending/dummy"));
    }

    @Test
    public void sendEttersendingUtenVedleggSkalFeile() throws Exception {
        when(soknadService.hentSoknad(BEHANDLINGS_ID, true, true)).thenReturn(soknad().medDelstegStatus(DelstegStatus.ETTERSENDING_OPPRETTET));
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn("<html></html>");

        try {
            actions.sendSoknad(BEHANDLINGS_ID, context);
            fail("Ettersending uten vedlegg skal feile");
        } catch (OpplastingException e) {
            // OK
        }
    }

    @Test
    public void sendSoknadMedOpprettetAnnetUtenOpplastingSkalFeile() throws Exception {
        when(soknadService.hentSoknad(BEHANDLINGS_ID, true, true)).thenReturn(soknadMedPlanlagtAnnetVedlegg().medDelstegStatus(DelstegStatus.OPPRETTET));
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn("<html></html>");

        try {
            actions.sendSoknad(BEHANDLINGS_ID, context);
            fail("Soknad der planlagt annet vedlegg ikke er lastet opp skal feile");
        } catch (OpplastingException e) {
            // OK
        }
    }

    @Test
    public void soknadBekreftelseEpostSkalInneholdeSoknadbekreftelseTekst() {
        Faktum sprakFaktum = new Faktum().medKey("skjema.sprak").medValue("nb_NO");
        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad().medFaktum(sprakFaktum));
        SoknadBekreftelse soknadBekreftelse = lagSoknadBekreftelse(false, true);

        actions.sendEpost(BEHANDLINGS_ID, "nb_NO", soknadBekreftelse, new MockHttpServletRequest());
        verify(tekster).finnTekst(eq("sendtSoknad.sendEpost.epostInnhold"), any(Object[].class), any(Locale.class));
    }

    @Test
    public void soknadBekreftelseEpostSkalBrukeNorskSomDefaultLocale() {
        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad());
        SoknadBekreftelse soknadBekreftelse = lagSoknadBekreftelse(false, true);

        actions.sendEpost(BEHANDLINGS_ID, "nb_NO", soknadBekreftelse, new MockHttpServletRequest());

        verify(tekster).finnTekst(eq("sendtSoknad.sendEpost.epostInnhold"), any(Object[].class), eq(new Locale("nb", "NO")));
    }


    @Test
    public void soknadBekreftelseEpostSkalSendeRettParametreTilEpostForTypeSoknadsdialoger() {
        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);

        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad());
        SoknadBekreftelse soknadBekreftelse = lagSoknadBekreftelse(false, true);
        soknadBekreftelse.setTemaKode("TSR");

        actions.sendEpost(BEHANDLINGS_ID, "nb_NO", soknadBekreftelse, new MockHttpServletRequest());

        verify(tekster).finnTekst(eq("sendtSoknad.sendEpost.epostInnhold"), captor.capture(), eq(new Locale("nb", "NO")));
        assertThat(captor.getValue()).containsSequence("/saksoversikt/app/tema/TSR", "/saksoversikt/app/ettersending");
    }

    @Test
    public void soknadBekreftelseEpostSkalSendeRettParametreTilEpostForTypeDokumentinnsending() {
        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);

        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad());
        SoknadBekreftelse soknadBekreftelse = lagSoknadBekreftelse(false, false);
        soknadBekreftelse.setTemaKode("KON");

        actions.sendEpost(BEHANDLINGS_ID, "nb_NO", soknadBekreftelse, new MockHttpServletRequest());

        verify(tekster).finnTekst(eq("sendtSoknad.sendEpost.epostInnhold"), captor.capture(), eq(new Locale("nb", "NO")));
        assertThat(captor.getValue()).containsSequence("/saksoversikt/app/tema/KON", "/saksoversikt/app/ettersending");
    }

    @Test
    public void soknadBekreftelseEpostSkalSendeRettParametreTilEpostSoknadsdialogerOgEttersendelse() {
        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);

        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad());
        SoknadBekreftelse soknadBekreftelse = lagSoknadBekreftelse(true, true);
        soknadBekreftelse.setTemaKode("TSR");

        actions.sendEpost(BEHANDLINGS_ID, "nb_NO", soknadBekreftelse, new MockHttpServletRequest());

        verify(tekster).finnTekst(eq("sendEttersendelse.sendEpost.epostInnhold"), captor.capture(), eq(new Locale("nb", "NO")));
        assertThat(captor.getValue()).containsSequence("/saksoversikt/app/tema/TSR");
    }

    @Test
    public void ettersendingBekreftelseEpostSkalInneholdeEttersendingbekreftelseTekst() {
        SoknadBekreftelse soknadBekreftelse = lagSoknadBekreftelse(true, true);

        actions.sendEpost("123", "nb_NO", soknadBekreftelse, new MockHttpServletRequest());

        verify(tekster).finnTekst(eq("sendEttersendelse.sendEpost.epostInnhold"), any(Object[].class), any(Locale.class));
    }

    @Test
    public void fortsettSenereEpostSkalInneholdeLenkeTilSoknadinnsending() {
        actions.sendEpost("123", new FortsettSenere(),new MockHttpServletRequest());

        ArgumentCaptor<Object[]> argumentCaptor = ArgumentCaptor.forClass(Object[].class);

        verify(tekster).finnTekst(eq("fortsettSenere.sendEpost.epostInnhold"), argumentCaptor.capture(), any(Locale.class));
        assertThat(argumentCaptor.getValue()[0]).isEqualTo(SOKNADINNSENDING_URL + "/soknad/123?utm_source=web&utm_medium=email&utm_campaign=2");
    }

    @Test
    public void fortsettSenereEpostSkalInneholdeLenkeTilEgenSoknadinnsending() {
        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(soknad().medskjemaNummer(new SoknadTilleggsstonader().getSkjemanummer().get(0)));

        actions.sendEpost("123", new FortsettSenere(),new MockHttpServletRequest());

        ArgumentCaptor<Object[]> argumentCaptor = ArgumentCaptor.forClass(Object[].class);

        verify(tekster).finnTekst(eq("fortsettSenere.sendEpost.epostInnhold"), argumentCaptor.capture(), any(Locale.class));
        assertThat(argumentCaptor.getValue()[0]).isEqualTo(SOKNADINNSENDING_URL + "/soknad/123?utm_source=web&utm_medium=email&utm_campaign=2");
    }

    private SoknadBekreftelse lagSoknadBekreftelse(boolean erEttersendelse, boolean erSoknadsdialog) {
        SoknadBekreftelse soknadBekreftelse = new SoknadBekreftelse();
        soknadBekreftelse.setEpost("test@nav.no");
        soknadBekreftelse.setErEttersendelse(erEttersendelse);
        soknadBekreftelse.setErSoknadsdialog(erSoknadsdialog);
        return soknadBekreftelse;
    }

    private WebSoknad soknad() {
        return new WebSoknad().medBehandlingId(BEHANDLINGS_ID).medskjemaNummer(new SoknadTilleggsstonader().getSkjemanummer().get(0));
    }

    private WebSoknad soknadMedVedlegg() {
        List<Vedlegg> vedlegg = asList(
                new Vedlegg()
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("uidVedlegg1")
                        .medInnsendingsvalg(Vedlegg.Status.VedleggKreves)
                        .medData(new byte[10])
                        .medStorrelse(10L)
                        .medNavn("Test Annet vedlegg")
                        .medAntallSider(3)
                        .medInnsendingsvalg(Vedlegg.Status.LastetOpp),
                new Vedlegg()
                        .medSkjemaNummer(SKJEMANUMMER_KVITTERING)
                        .medInnsendingsvalg(Vedlegg.Status.SendesIkke));

        return new WebSoknad()
                .medBehandlingId(BEHANDLINGS_ID).medskjemaNummer("NAV 11-12.12")
                .medVedlegg(vedlegg)
                .medFaktum(new Faktum().medKey("personalia"));
    }

    private WebSoknad soknadMedPlanlagtAnnetVedlegg() {
        List<Vedlegg> vedlegg = asList(
                new Vedlegg()
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("uidVedlegg1")
                        .medInnsendingsvalg(Vedlegg.Status.VedleggKreves)
                        .medStorrelse(0L)
                        .medNavn("Test Annet vedlegg")
                        .medAntallSider(3)
                        .medInnsendingsvalg(Vedlegg.Status.VedleggKreves),
                new Vedlegg()
                        .medSkjemaNummer(SKJEMANUMMER_KVITTERING)
                        .medInnsendingsvalg(Vedlegg.Status.SendesIkke));

        return new WebSoknad()
                .medBehandlingId(BEHANDLINGS_ID).medskjemaNummer("NAV 11-12.12")
                .medVedlegg(vedlegg)
                .medFaktum(new Faktum().medKey("personalia"));
    }
}
