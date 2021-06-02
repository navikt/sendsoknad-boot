package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.Steg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.EkstraMetadataTransformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Steg.*;

public abstract class KravdialogInformasjon {

    public static final int DEFAULT_VERSJON = 0;
    static final String BOLK_PERSONALIA = "Personalia";
    static final String BOLK_BARN = "Barn";

    private final List<String> skjemanummer;


    KravdialogInformasjon(List<String> skjemanummer) {
        this.skjemanummer = skjemanummer;
    }

    public Steg[] getStegliste() {
        return new Steg[]{VEILEDNING, SOKNAD, VEDLEGG, OPPSUMMERING};
    }

    public List<AlternativRepresentasjonTransformer> getTransformers(TekstHenter tekstHenter, WebSoknad soknad) {
        return new ArrayList<>();
    }

    public List<EkstraMetadataTransformer> getMetadataTransformers() {
        return Collections.emptyList();
    }

    public boolean brukerNyOppsummering(){
        return false;
    }

    public boolean skalSendeMedFullSoknad(){
        return false;
    }

    public SoknadType getSoknadstype() {
        return SoknadType.SEND_SOKNAD;
    }

    public String getKvitteringTemplate() {
        return "/skjema/kvittering";
    }

    public Integer getSkjemaVersjon() { return DEFAULT_VERSJON; }

    public List<String> getSkjemanummer() {
        return skjemanummer;
    }


    public abstract String getSoknadTypePrefix();

    public abstract String getSoknadUrlKey();

    public abstract String getFortsettSoknadUrlKey();

    public abstract String getStrukturFilnavn();

    public abstract String getBundleName();

    public abstract List<String> getSoknadBolker(WebSoknad soknad);
}
