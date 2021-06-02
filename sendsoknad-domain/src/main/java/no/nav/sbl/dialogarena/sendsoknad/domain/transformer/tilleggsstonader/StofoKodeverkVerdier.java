package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import java.util.function.Function;

public class StofoKodeverkVerdier {
    public static final class SammensattAdresse {
        private static final String NORGE = "Norge";

        public final String sammensattAdresse;

        public SammensattAdresse(String land, String adresse, String postnr, String utenlandskAdresse) {
            if (land == null || land.equals(NORGE)) {
                sammensattAdresse = String.format("%s, %s", adresse, postnr);
            } else {
                sammensattAdresse = String.format("%s, %s", utenlandskAdresse, land);
            }
        }
    }

    public enum BarnepassAarsak{
        langvarig("soknad.barnepass.fjerdeklasse.aarsak.langvarig"),
        trengertilsyn("soknad.barnepass.fjerdeklasse.aarsak.tilsyn"),
        ingen("soknad.barnepass.fjerdeklasse.aarsak.ingen");
        public final String cmsKey;

        BarnepassAarsak(String cmsKey) {
            this.cmsKey = cmsKey;
        }
    }

    public enum FormaalKodeverk {
        oppfolging("OPPF"), jobbintervju("JOBB"), tiltraa("TILT");
        public final String kodeverksverdi;

        FormaalKodeverk(String kodeverksverdi) {
            this.kodeverksverdi = kodeverksverdi;
        }
    }

    public enum SkolenivaaerKodeverk {
        videregaende("VGS"), hoyereutdanning("HGU"), annet("ANN");

        public final String kodeverk;

        SkolenivaaerKodeverk(String kodeverk) {
            this.kodeverk = kodeverk;
        }
    }

    public enum ErUtgifterDekketKodeverk {
        ja("JA"), nei("NEI"), delvis("DEL");

        public final String kodeverk;

        ErUtgifterDekketKodeverk(String kodeverk) {
            this.kodeverk = kodeverk;
        }
    }

    public enum InnsendingsintervallerKodeverk {
        uke("UKE"), maned("MND");
        public final String kodeverksverdi;

        InnsendingsintervallerKodeverk(String kodeverksverdi) {
            this.kodeverksverdi = kodeverksverdi;
        }

    }
    public enum FlytterSelv {
        flytterselv ("soknad.flytting.spm.selvellerbistand.flytterselv"),
        flyttebyraa("soknad.flytting.spm.selvellerbistand.flyttebyraa"),
        tilbudmenflytterselv("soknad.flytting.spm.selvellerbistand.tilbudmenflytterselv");
        public final String cms;

        FlytterSelv(String cms) {
            this.cms = cms;
        }
    }

    public enum TilsynForetasAv {
        privat("Privat"), offentlig("Offentlig"), annet("Annet");
        public final String stofoString;

        TilsynForetasAv(String stofoString) {
            this.stofoString = stofoString;
        }

        public static final Function<String, String> TO_TILSYN_FORETAS_AV_ENUM = kodeverk -> {
            try {
                return TilsynForetasAv.valueOf(kodeverk).stofoString;
            } catch (IllegalArgumentException ignore) {
                return "";
            }
        };
    }

    public enum TilsynForetasAvKodeverk{
        dagmamma("KOM"), barnehage("OFF"), privat("PRI");
        public final String kodeverksverdi;

        TilsynForetasAvKodeverk(String kodeverksverdi) {
            this.kodeverksverdi = kodeverksverdi;
        }

    }
}
