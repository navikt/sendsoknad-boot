package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BilstonadInformasjon extends KravdialogInformasjon {

    public BilstonadInformasjon() {
        super(Arrays.asList("NAV 10-07.40", "NAV 10-07.41"));
    }

    @Override
    public String getSoknadTypePrefix() {
        return "bilstonad";
    }

    @Override
    public String getSoknadUrlKey() {
        return "bilstonad.path";
    }

    @Override
    public String getFortsettSoknadUrlKey() {
        return "bilstonad.path";
    }

    @Override
    public String getStrukturFilnavn() {
        return "bilstonad.xml";
    }

    @Override
    public String getBundleName() {
        return "bilstonad";
    }

    @Override
    public List<String> getSoknadBolker(WebSoknad soknad) {
        return Collections.singletonList(BOLK_PERSONALIA);
    }
}
