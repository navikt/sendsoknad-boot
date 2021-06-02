package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Maalgruppeinformasjon;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Maalgruppetyper;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoTransformers;

public class MaalgruppeTilXml {

    public static Maalgruppeinformasjon transform(Faktum faktum) {
        Maalgrupper maalgruppe = Maalgrupper.toMaalgruppe(faktum.getProperties().get("kodeverkVerdi"));
        if (maalgruppe == null) {
            return null;
        }

        Maalgruppeinformasjon informasjon = new Maalgruppeinformasjon();
        informasjon.setPeriode(StofoTransformers.extractValue(faktum, Periode.class));
        informasjon.setMaalgruppetype(lagType(maalgruppe));
        informasjon.setKilde(faktum.getType().toString());
        return informasjon;
    }

    private static Maalgruppetyper lagType(Maalgrupper maalgruppe) {
        Maalgruppetyper type = new Maalgruppetyper();
        type.setKodeverksRef(maalgruppe.name());
        type.setValue(maalgruppe.name());
        return type;
    }

    private enum Maalgrupper {
        NEDSARBEVN,
        ENSFORUTD,
        ENSFORARBS,
        TIDLFAMPL,
        GJENEKUTD,
        GJENEKARBS,
        MOTTILTPEN,
        MOTDAGPEN,
        ARBSOKERE;

        public static Maalgrupper toMaalgruppe(String kodeverkVerdi) {
            try {
                return valueOf(kodeverkVerdi != null ? kodeverkVerdi.toUpperCase() : "");
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
