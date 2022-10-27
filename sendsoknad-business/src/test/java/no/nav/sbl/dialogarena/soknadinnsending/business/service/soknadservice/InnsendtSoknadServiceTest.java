package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.InnsendtSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import org.assertj.core.api.Condition;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InnsendtSoknadServiceTest {

    private static final XMLHovedskjema HOVEDSKJEMA = new XMLHovedskjema()
            .withSkjemanummer("NAV 11-12.12")
            .withInnsendingsvalg("LASTET_OPP");
    private static final String SPRAK = "no_NB";

    private XMLMetadataListe xmlMetadataListe;
    private XMLHenvendelse xmlHenvendelse;

    //private WebSoknad webSoknad;
    private List<Vedlegg> vedleggsListe = new LinkedList<>();

    private Vedlegg hovedVedlegg = new Vedlegg()
            .medVedleggId(1L)
            .medSoknadId(1L)
            .medNavn("Hovedskjema")
            .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
            .medSkjemaNummer("NAV 11-12.12");
    private Vedlegg kvitteringsVedlegg = new Vedlegg()
            .medVedleggId(2L)
            .medSoknadId(1L)
            .medNavn("Kvittering")
            .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
            .medSkjemaNummer(SKJEMANUMMER_KVITTERING);
    private Vedlegg annetVedlegg = new Vedlegg()
            .medVedleggId(3L)
            .medSoknadId(1L)
            .medNavn("Noe annet")
            .medInnsendingsvalg(Vedlegg.Status.SendesSenere)
            .medSkjemaNummer("N6");
    private Vedlegg avAndreVedlegg = new Vedlegg()
            .medVedleggId(4L)
            .medSoknadId(1L)
            .medNavn("C2 skjema")
            .medInnsendingsvalg(Vedlegg.Status.VedleggSendesAvAndre)
            .medSkjemaNummer("C2");
    private Vedlegg sendesIkkeVedlegg = new Vedlegg()
            .medVedleggId(5L)
            .medSoknadId(1L)
            .medNavn("Annet skjema")
            .medInnsendingsvalg(Vedlegg.Status.SendesIkke)
            .medSkjemaNummer("N6");
    private Vedlegg alleredeSendtVedlegg = new Vedlegg()
            .medVedleggId(5L)
            .medSoknadId(1L)
            .medNavn("X2 skjema")
            .medInnsendingsvalg(Vedlegg.Status.VedleggAlleredeSendt)
            .medSkjemaNummer("X2");

    @Mock
    private HenvendelseService henvendelseService;

    @SuppressWarnings("unused")
    @Mock
    private VedleggService vedleggService;

    @Mock
    private SoknadRepository lokalDb;

    @InjectMocks
    private InnsendtSoknadService service;

    @Before
    public void setUp() {
        xmlHenvendelse = new XMLHenvendelse();
        xmlMetadataListe = new XMLMetadataListe();

        if (!SoknadDataFletter.GCP_ARKIVERING_ENABLED) {
            when(henvendelseService.hentInformasjonOmAvsluttetSoknad(anyString())).thenReturn(
                    xmlHenvendelse.withMetadataListe(xmlMetadataListe));
        }

        vedleggsListe.add(hovedVedlegg);
        vedleggsListe.add(kvitteringsVedlegg);
        vedleggsListe.add(sendesIkkeVedlegg);
        vedleggsListe.add(avAndreVedlegg);
        vedleggsListe.add(annetVedlegg);
        vedleggsListe.add(alleredeSendtVedlegg);

        //webSoknad.setInnsendtDato(DateTime.now());

    }

    @Test
    public void skalFjerneKvitteringerFraVedleggene() {
        xmlMetadataListe.withMetadata(
                HOVEDSKJEMA,
                new XMLVedlegg()
                        .withInnsendingsvalg("LASTET_OPP")
                        .withSkjemanummer(SKJEMANUMMER_KVITTERING));
        WebSoknad webSoknad = new WebSoknad()
                .medAktorId("1234")
                .medInnsendtDato(Timestamp.valueOf(LocalDateTime.now()))
                .medBehandlingId("ID01")
                .medId(1)
                .medskjemaNummer("NAV 11-12.12")
                .medVedlegg(vedleggsListe);
        when(lokalDb.hentSoknadMedVedlegg(anyString())).thenReturn(webSoknad.medVedlegg(vedleggsListe));

        InnsendtSoknad soknad = service.hentInnsendtSoknad("ID01", SPRAK);
        assertThat(soknad.getIkkeInnsendteVedlegg()).areNot(liktSkjemanummer(SKJEMANUMMER_KVITTERING));
        assertThat(soknad.getInnsendteVedlegg()).areNot(liktSkjemanummer(SKJEMANUMMER_KVITTERING));
    }

    @Test
    public void skalPlassereOpplastetVedleggUnderInnsendteVedlegg() {
        xmlMetadataListe.withMetadata(HOVEDSKJEMA);
        WebSoknad webSoknad = new WebSoknad()
                .medAktorId("1234")
                .medInnsendtDato(Timestamp.valueOf(LocalDateTime.now()))
                .medBehandlingId("ID01")
                .medId(1)
                .medskjemaNummer("NAV 11-12.12")
                .medVedlegg(vedleggsListe);
        when(lokalDb.hentSoknadMedVedlegg(anyString())).thenReturn(webSoknad.medVedlegg(vedleggsListe));

        InnsendtSoknad soknad = service.hentInnsendtSoknad("ID01", SPRAK);
        assertThat(soknad.getInnsendteVedlegg()).are(liktSkjemanummer(HOVEDSKJEMA.getSkjemanummer()));
        //assertThat(soknad.getIkkeInnsendteVedlegg()).hasSize(0);
    }

    @Test
    public void skalMappeDetaljerFraHenvendelse() {
        if (!SoknadDataFletter.GCP_ARKIVERING_ENABLED) {
            xmlMetadataListe.withMetadata(HOVEDSKJEMA);
            xmlHenvendelse
                    .withAvsluttetDato(new DateTime(2016, 1, 1, 12, 0))
                    .withTema("TSO");

            InnsendtSoknad soknad = service.hentInnsendtSoknad("ID01", SPRAK);
            assertThat(soknad.getDato()).isEqualTo("1. januar 2016");
            assertThat(soknad.getKlokkeslett()).isEqualTo("12.00");
            assertThat(soknad.getTemakode()).isEqualToIgnoringCase("TSO");
        }
    }

    @Test
    public void skalMappeDetaljerFraHenvendelsePaEngelsk() {
        if (!SoknadDataFletter.GCP_ARKIVERING_ENABLED) {
            xmlMetadataListe.withMetadata(HOVEDSKJEMA);
            xmlHenvendelse
                    .withAvsluttetDato(new DateTime(2016, 1, 1, 12, 0))
                    .withTema("TSO");

            InnsendtSoknad soknad = service.hentInnsendtSoknad("ID01", "en");
            assertThat(soknad.getDato()).isEqualTo("1. January 2016");
            assertThat(soknad.getKlokkeslett()).isEqualTo("12.00");
        }
    }

    @Test
    public void skalPlassereIkkeOpplastetVedleggUnderIkkeInnsendteVedlegg() {
        Collection<XMLMetadata> ikkeInnsendteVedlegg = Arrays.asList(
                new XMLVedlegg().withInnsendingsvalg("VEDLEGG_SENDES_AV_ANDRE"),
                new XMLVedlegg().withInnsendingsvalg("SEND_SENERE"),
                new XMLVedlegg().withInnsendingsvalg("VEDLEGG_ALLEREDE_SENDT"),
                new XMLVedlegg().withInnsendingsvalg("VEDLEGG_SENDES_IKKE"));
        xmlMetadataListe.withMetadata(HOVEDSKJEMA);
        xmlMetadataListe.withMetadata(ikkeInnsendteVedlegg);

        WebSoknad webSoknad = new WebSoknad()
                .medAktorId("1234")
                .medInnsendtDato(Timestamp.valueOf(LocalDateTime.now()))
                .medBehandlingId("ID01")
                .medId(1)
                .medskjemaNummer("NAV 11-12.12")
                .medVedlegg(vedleggsListe);
        when(lokalDb.hentSoknadMedVedlegg(anyString())).thenReturn(webSoknad.medVedlegg(vedleggsListe));

        InnsendtSoknad soknad = service.hentInnsendtSoknad("ID01", SPRAK);
        assertThat(soknad.getInnsendteVedlegg()).hasSize(1);
        assertThat(soknad.getIkkeInnsendteVedlegg()).hasSameSizeAs(ikkeInnsendteVedlegg);
    }

    @Test
    public void skalKasteExceptionOmHovedskjemaMangler() {
        xmlMetadataListe.withMetadata(new XMLMetadata());
        WebSoknad webSoknad = new WebSoknad()
                .medAktorId("1234")
                .medInnsendtDato(Timestamp.valueOf(LocalDateTime.now()))
                .medBehandlingId("ID01")
                .medId(1)
                .medskjemaNummer("NAV 11-12.12")
                .medVedlegg(new LinkedList<>());

        when(lokalDb.hentSoknadMedVedlegg(anyString())).thenReturn(webSoknad.medVedlegg(new LinkedList<>()));

        try {
            service.hentInnsendtSoknad("ID01", SPRAK);
            fail("Skal kaste exception når Hovedskjema mangler");
        } catch (SendSoknadException e) {
            // Expected this exception
        } catch (Exception e) {
            fail("Did not expect this type of exception");
        }
    }

    private Condition<Vedlegg> liktSkjemanummer(final String skjemanummer) {
        return new Condition<Vedlegg>() {
            @Override
            public boolean matches(Vedlegg vedlegg) {
                return skjemanummer.equals(vedlegg.getSkjemaNummer());
            }
        };
    }
}
