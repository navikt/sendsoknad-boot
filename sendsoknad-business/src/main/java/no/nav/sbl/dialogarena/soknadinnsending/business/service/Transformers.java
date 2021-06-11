package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import org.joda.time.LocalDate;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

public class Transformers {
    private static final Logger logger = getLogger(Transformers.class);

    public static final String KONTRAKT_UTGAATT = "kontraktutgaatt";
    public static final String AVSKJEDIGET = "avskjediget";
    public static final String REDUSERT_ARBEIDSTID = "redusertarbeidstid";
    public static final String ARBEIDSGIVER_ERKONKURS = "arbeidsgivererkonkurs";
    public static final String SAGTOPP_AV_ARBEIDSGIVER = "sagtoppavarbeidsgiver";
    public static final String SAGTOPP_SELV = "sagtoppselv";


    public static final Function<Faktum, LocalDate> DATO_TIL = faktum -> {
        Map<String, String> properties = faktum.getProperties();
        switch (faktum.getProperties().get("type")) {
            case REDUSERT_ARBEIDSTID:
                return new LocalDate(properties.get("redusertfra"));
            case ARBEIDSGIVER_ERKONKURS:
                return new LocalDate(properties.get("konkursdato"));
            case KONTRAKT_UTGAATT:
            case AVSKJEDIGET:
            case SAGTOPP_AV_ARBEIDSGIVER:
            case SAGTOPP_SELV:
                return new LocalDate(properties.get("datotil"));
            default:
                return null;
        }
    };

    public static XMLVedlegg[] convertToXmlVedleggListe(List<Vedlegg> vedleggForventnings, SkjemaOppslagService skjemaOppslagService) {
        List<XMLVedlegg> resultat = new ArrayList<>();
        for (Vedlegg vedlegg : vedleggForventnings) {
            XMLVedlegg xmlVedlegg;
            if (vedlegg.getInnsendingsvalg().er(LastetOpp)) {
                xmlVedlegg = new XMLVedlegg()
                        .withFilnavn(vedlegg.lagFilNavn())
                        .withSideantall(vedlegg.getAntallSider())
                        .withMimetype(isEmpty(vedlegg.getMimetype()) ? "application/pdf" : vedlegg.getMimetype())
                        .withTilleggsinfo(finnVedleggsnavn(vedlegg, skjemaOppslagService))
                        .withFilstorrelse(vedlegg.getStorrelse().toString())
                        .withSkjemanummer(vedlegg.getSkjemaNummer())
                        .withUuid(vedlegg.getFillagerReferanse())
                        .withInnsendingsvalg(LASTET_OPP.value());
            } else {
                xmlVedlegg = new XMLVedlegg()
                        .withFilnavn(vedlegg.lagFilNavn())
                        .withTilleggsinfo(vedlegg.getNavn())
                        .withSkjemanummer(vedlegg.getSkjemaNummer())
                        .withInnsendingsvalg(toXmlInnsendingsvalg(vedlegg.getInnsendingsvalg()));
            }
            String skjemanummerTillegg = vedlegg.getSkjemanummerTillegg();
            if (isNotBlank(skjemanummerTillegg)) {
                xmlVedlegg.setSkjemanummerTillegg(skjemanummerTillegg);
            }
            resultat.add(xmlVedlegg);
        }
        return resultat.toArray(new XMLVedlegg[0]);
    }

    private static String finnVedleggsnavn(Vedlegg vedlegg, SkjemaOppslagService skjemaOppslagService) {
        if ("N6".equalsIgnoreCase(vedlegg.getSkjemaNummer()) && vedlegg.getNavn() != null && !"".equals(vedlegg.getNavn())) {
            return vedlegg.getNavn();
        }
        String skjemanummerTillegg = "";
        if (vedlegg.getSkjemanummerTillegg() != null && !"".equals(vedlegg.getSkjemanummerTillegg())) {
            skjemanummerTillegg = ": " + vedlegg.getSkjemanummerTillegg();
        }

        try {
            String skjemaNavn = skjemaOppslagService.getTittel(vedlegg.getSkjemaNummer());
            return skjemaNavn + skjemanummerTillegg;

        } catch (Exception e) {
            logger.warn("Unable to find tittel for '{}'", vedlegg.getSkjemaNummer());
            return null;
        }
    }

    public static String toXmlInnsendingsvalg(Vedlegg.Status innsendingsvalg) {
        switch (innsendingsvalg) {
            case LastetOpp:
                return LASTET_OPP.toString();
            case SendesSenere:
                return SEND_SENERE.toString();
            case VedleggSendesAvAndre:
                return VEDLEGG_SENDES_AV_ANDRE.toString();
            case VedleggSendesIkke:
                return VEDLEGG_SENDES_IKKE.toString();
            case VedleggAlleredeSendt:
                return VEDLEGG_ALLEREDE_SENDT.toString();
            case SendesIkke:
            default:
                return SENDES_IKKE.toString();
        }
    }

    public static Vedlegg.Status toInnsendingsvalg(String xmlInnsendingsvalg) {
        switch (xmlInnsendingsvalg) {
            case "LASTET_OPP":
                return LastetOpp;
            case "SEND_SENERE":
                return SendesSenere;
            case "VEDLEGG_SENDES_IKKE":
                return VedleggSendesIkke;
            case "VEDLEGG_SENDES_AV_ANDRE":
                return VedleggSendesAvAndre;
            case "VEDLEGG_ALLEREDE_SENDT":
                return VedleggAlleredeSendt;
            case "SENDES_IKKE":
            default:
                return SendesIkke;
        }
    }
}
