package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TiltakspengerInformasjon extends KravdialogInformasjon {

    TiltakspengerInformasjon() {
        super(Collections.singletonList("NAV 76-13.45"));
    }

    @Override
    public String getSoknadTypePrefix() {
        return "tiltakspenger";
    }

    @Override
    public String getSoknadUrlKey() {
        return "tiltakspenger.path";
    }

    @Override
    public String getFortsettSoknadUrlKey() {
        return "tiltakspenger.path";
    }

    @Override
    public String getStrukturFilnavn() {
        return "tiltakspenger.xml";
    }

    @Override
    public String getBundleName() {
        return "tiltakspenger";
    }

    @Override
    public List<String> getSoknadBolker(WebSoknad soknad) {
        return Arrays.asList(BOLK_PERSONALIA, BOLK_BARN);
    }
}
