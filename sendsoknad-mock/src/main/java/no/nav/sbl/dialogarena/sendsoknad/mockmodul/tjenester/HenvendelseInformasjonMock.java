package no.nav.sbl.dialogarena.sendsoknad.mockmodul.tjenester;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.*;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.henvendelse.HenvendelsePortType;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentHenvendelseListeRequest;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentHenvendelseListeResponse;
import org.joda.time.DateTime;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HenvendelseInformasjonMock {

    private static final String SKJEMANUMMER_KVITTERING = "L7";
    private static final String BILSTONAD = "NAV 10-07.40";
    private static final String AAP_SKJEMAKODE_NAV_11_13_05 = "NAV 11-13.05";
    private static final String BEHANDLINGSID = "behandlingsidX";
    private static final String AAP_BEHANDLINGSID = "xxxx-mockbehandlingid2";
    private static final String ETTERSENDELSE_BEHANDLINGSID = "ettersendingbehandlingsid2";
    private static final String AAP_ETTERSENDELSE_BEHANDLINGSID = "ettersendingbehandlingsid1";

    public static HenvendelsePortType getHenvendelseSoknaderPortTypeMock() {

        HenvendelsePortType mock = mock(HenvendelsePortType.class);
        when(mock.hentHenvendelseListe(any(WSHentHenvendelseListeRequest.class))).thenAnswer(invocationOnMock -> soknadListe());
        return mock;
    }

    private static XMLHenvendelse gammelFerdigWSSoknad() {
        return new XMLHenvendelse()
                .withBehandlingsId("behandlingId1")
                .withJournalfortInformasjon(new XMLJournalfortInformasjon().withJournalpostId("348274526"))
                .withHenvendelseType(XMLHenvendelseType.DOKUMENTINNSENDING.toString())
                .withOpprettetDato(DateTime.now().minusDays(41))
                .withAvsluttetDato(DateTime.now().minusDays(40))
                .withMetadataListe(new XMLMetadataListe().withMetadata(
                        new XMLHovedskjema()
                                .withSkjemanummer("NAV 04-01.05"),
                        new XMLVedlegg()
                                .withSkjemanummer(BILSTONAD)
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("348128630")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer(SKJEMANUMMER_KVITTERING)
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("358128632")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer("NAV 03-16.10")
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("358128630")
                                .withInnsendingsvalg(XMLInnsendingsvalg.SEND_SENERE.value())
                ));
    }

    private static WSHentHenvendelseListeResponse soknadListe() {
        return new WSHentHenvendelseListeResponse().withAny(
                kvitteringDokInnsending(),
                kvitteringEttersendelseDokInnsending(),
                kvitteringSendsoknad(),
                kvitteringEttersendelseSendsoknad(),
                gammelFerdigWSSoknad()
        );
    }

    private static XMLHenvendelse kvitteringDokInnsending() {
        return new XMLHenvendelse()
                .withBehandlingsId(AAP_BEHANDLINGSID)
                .withHenvendelseType(XMLHenvendelseType.DOKUMENTINNSENDING.toString())
                .withMetadataListe(new XMLMetadataListe().withMetadata(
                        new XMLHovedskjema()
                                .withSkjemanummer(AAP_SKJEMAKODE_NAV_11_13_05)))
                .withOpprettetDato(DateTime.now().minusYears(1).minusDays(29))
                .withAvsluttetDato(DateTime.now().minusYears(1));
    }

    private static XMLHenvendelse kvitteringEttersendelseDokInnsending() {
        return new XMLHenvendelse()
                .withBehandlingsId(AAP_ETTERSENDELSE_BEHANDLINGSID)
                .withJournalfortInformasjon(new XMLJournalfortInformasjon().withJournalpostId("348274526"))
                .withHenvendelseType(XMLHenvendelseType.DOKUMENTINNSENDING.toString())
                .withMetadataListe(new XMLMetadataListe().withMetadata(
                        new XMLHovedskjema()
                                .withSkjemanummer(AAP_SKJEMAKODE_NAV_11_13_05),
                        new XMLVedlegg()
                                .withSkjemanummer(BILSTONAD)
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("368128630")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer(BILSTONAD)
                                .withUuid(randomUUID().toString())
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value())
                        ,
                        new XMLVedlegg()
                                .withSkjemanummer(SKJEMANUMMER_KVITTERING)
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("358128632")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer("NAV 00-01.01")
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("378128630")
                                .withInnsendingsvalg(XMLInnsendingsvalg.SENDES_IKKE.value())
                ))
                .withOpprettetDato(DateTime.now().minusDays(2))
                .withAvsluttetDato(DateTime.now());
    }


    private static XMLHenvendelse kvitteringEttersendelseSendsoknad() {
        return new XMLHenvendelse()
                .withBehandlingsId(ETTERSENDELSE_BEHANDLINGSID)
                .withJournalfortInformasjon(new XMLJournalfortInformasjon().withJournalpostId("368274526"))
                .withHenvendelseType(XMLHenvendelseType.SEND_SOKNAD.toString())
                .withOpprettetDato(DateTime.now().minusDays(3))
                .withAvsluttetDato(DateTime.now())
                .withLestDato(DateTime.now().minusDays(2))
                .withMetadataListe(new XMLMetadataListe().withMetadata(
                        new XMLHovedskjema()
                                .withSkjemanummer(BILSTONAD),
                        new XMLVedlegg()
                                .withSkjemanummer(BILSTONAD)
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("388128630")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer("NAV 03-16.10")
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("398128630")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer(SKJEMANUMMER_KVITTERING)
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("358128632")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value())
                ));
    }

    private static XMLHenvendelse kvitteringSendsoknad() {
        return new XMLHenvendelse()
                .withBehandlingsId(BEHANDLINGSID)
                .withJournalfortInformasjon(new XMLJournalfortInformasjon().withJournalpostId("368274540"))
                .withHenvendelseType(XMLHenvendelseType.SEND_SOKNAD.toString())
                .withOpprettetDato(DateTime.now().minusDays(6))
                .withLestDato(DateTime.now())
                .withAvsluttetDato(DateTime.now().minusDays(5))
                .withMetadataListe(new XMLMetadataListe().withMetadata(
                        new XMLHovedskjema()
                                .withSkjemanummer(BILSTONAD),
                        new XMLVedlegg()
                                .withSkjemanummer(BILSTONAD)
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("378128640")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer(SKJEMANUMMER_KVITTERING)
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("358128632")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer("NAV 03-16.10")
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("378128650")
                                .withInnsendingsvalg(XMLInnsendingsvalg.SEND_SENERE.value())
                ));
    }
}
