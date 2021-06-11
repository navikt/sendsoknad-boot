package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.*;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransformersTest {

    @Test
    public void skalMappeDatoType() {
        Faktum faktum = new Faktum()
                .medProperty("datotil", "2013-01-01")
                .medProperty("redusertfra", "2013-01-02")
                .medProperty("konkursdato", "2013-01-03")
                .medProperty("permiteringsperiodedatofra", "2013-01-04")
                .medProperty("type", KONTRAKT_UTGAATT);

        assertEquals(new LocalDate("2013-01-01"), Transformers.DATO_TIL.apply(faktum));
        assertEquals(new LocalDate("2013-01-01"), Transformers.DATO_TIL.apply(faktum.medProperty("type", AVSKJEDIGET)));
        assertEquals(new LocalDate("2013-01-02"), Transformers.DATO_TIL.apply(faktum.medProperty("type", REDUSERT_ARBEIDSTID)));
        assertEquals(new LocalDate("2013-01-03"), Transformers.DATO_TIL.apply(faktum.medProperty("type", ARBEIDSGIVER_ERKONKURS)));
        assertEquals(new LocalDate("2013-01-01"), Transformers.DATO_TIL.apply(faktum.medProperty("type", SAGTOPP_AV_ARBEIDSGIVER)));
        assertEquals(new LocalDate("2013-01-01"), Transformers.DATO_TIL.apply(faktum.medProperty("type", SAGTOPP_SELV)));
        assertNull(Transformers.DATO_TIL.apply(faktum.medProperty("type", "tullball")));
    }

    @Test
    public void skalKonvertereInnsendingsvalg() {
        assertEquals(LASTET_OPP.toString(), Transformers.toXmlInnsendingsvalg(Vedlegg.Status.LastetOpp));
        assertEquals(SENDES_IKKE.toString(), Transformers.toXmlInnsendingsvalg(Vedlegg.Status.SendesIkke));
        assertEquals(SEND_SENERE.toString(), Transformers.toXmlInnsendingsvalg(Vedlegg.Status.SendesSenere));
        assertEquals(SENDES_IKKE.toString(), Transformers.toXmlInnsendingsvalg(Vedlegg.Status.IkkeVedlegg));
        assertEquals(VEDLEGG_ALLEREDE_SENDT.toString(), Transformers.toXmlInnsendingsvalg(Vedlegg.Status.VedleggAlleredeSendt));
    }

    @Test
    public void skalKonvertereVedlegg() {
        List<VedleggData> vedleggsListe = asList(
                new VedleggData("H8", null), // Mangler pt. i listen over vedlegg
                new VedleggData("U8", "Dokumentasjon av reiseutgifter", "reisearbeidssoker"),
                new VedleggData("L7", "Kvitteringsside for dokumentinnsending"),
                new VedleggData("X3", "Kopi av likningsattest eller selvangivelse"),
                new VedleggData("V2", "Medisinsk dokumentasjon"),
                new VedleggData("N6", "Navn fra frontend")
        );
        List<Vedlegg> vedleggForventnings = createData(vedleggsListe);
        SkjemaOppslagService skjemaOppslagService = mockSkjemaOppslagService(vedleggsListe);

        XMLVedlegg[] xmlVedleggs = Transformers.convertToXmlVedleggListe(vedleggForventnings, skjemaOppslagService);

        assertTrue(xmlVedleggs.length > 5);
        assertNull(xmlVedleggs[0].getTilleggsinfo());
        assertTrue(xmlVedleggs[1].getTilleggsinfo().contains(vedleggsListe.get(1).title + ": reisearbeidssoker"));
        assertTrue(xmlVedleggs[2].getTilleggsinfo().contains(vedleggsListe.get(2).title));
        assertTrue(xmlVedleggs[3].getTilleggsinfo().contains(vedleggsListe.get(3).title));
        assertTrue(xmlVedleggs[4].getTilleggsinfo().contains(vedleggsListe.get(4).title));
        assertTrue(xmlVedleggs[5].getTilleggsinfo().contains(vedleggsListe.get(5).title));
    }

    private static List<Vedlegg> createData(List<VedleggData> vedleggsListe) {
        List<Vedlegg> vedleggForventnings = new ArrayList<>();

        for (VedleggData p : vedleggsListe) {
            vedleggForventnings.add(lagVedlegg(p.id, p.tillegg));
        }
        return vedleggForventnings;
    }

    private static SkjemaOppslagService mockSkjemaOppslagService(List<VedleggData> vedleggsListe) {
        SkjemaOppslagService skjemaOppslagService = mock(SkjemaOppslagService.class);

        for (VedleggData p : vedleggsListe) {
            if (p.title == null) {
                when(skjemaOppslagService.getTittel(eq(p.id))).thenThrow(new RuntimeException("Mocked exception -- no title in sanity"));
            } else {
                when(skjemaOppslagService.getTittel(eq(p.id))).thenReturn(p.title);
            }
        }
        return skjemaOppslagService;
    }

    private static Vedlegg lagVedlegg(String skjemaNummer, String skjemanummerTillegg) {
        return new Vedlegg()
                .medAarsak("Aarsak")
                .medAntallSider(1)
                .medFaktumId(10L)
                .medFillagerReferanse("Fillagerreferanse")
                .medFilnavn("Filnavn")
                .medSkjemaNummer(skjemaNummer)
                .medSkjemanummerTillegg(skjemanummerTillegg)
                .medNavn("Navn fra frontend")
                .medInnsendingsvalg(Vedlegg.Status.LastetOpp);
    }

    private static class VedleggData {
        String id;
        String title;
        String tillegg;

        VedleggData(String id, String title) {
            this(id, title, null);
        }

        VedleggData(String id, String title, String tillegg) {
            this.id = id;
            this.title = title;
            this.tillegg = tillegg;
        }
    }
}
