package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Collections;
import java.util.List;

public class AAPUtlandetInformasjon extends KravdialogInformasjon {

    public AAPUtlandetInformasjon() {
        super(Collections.singletonList("NAV 11-03.07"));
    }

    @Override
    public String getSoknadTypePrefix() {
        return "aap.utland";
    }

    @Override
    public String getSoknadUrlKey() {
        return "soknad.aap.utland.path";
    }

    @Override
    public String getFortsettSoknadUrlKey() {
        return "soknad.aap.utland.path";
    }

    @Override
    public String getStrukturFilnavn() {
        return "aap_utland.xml";
    }

    @Override
    public String getBundleName() {
        return "soknad-aap-utland";
    }

    @Override
    public List<String> getSoknadBolker(WebSoknad soknad) {
        return Collections.singletonList(BOLK_PERSONALIA);
    }
}
