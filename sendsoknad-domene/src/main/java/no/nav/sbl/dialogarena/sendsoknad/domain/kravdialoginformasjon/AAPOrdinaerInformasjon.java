package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AAPOrdinaerInformasjon extends KravdialogInformasjon {

    public AAPOrdinaerInformasjon() {
        super(Collections.singletonList("NAV 11-13.05"));
    }

    @Override
    public String getSoknadTypePrefix() {
        return "aap.ordinaer";
    }

    @Override
    public String getSoknadUrlKey() {
        return "soknad.aap.ordinaer.path";
    }

    @Override
    public String getFortsettSoknadUrlKey() {
        return "soknad.aap.fortsett.path";
    }

    @Override
    public String getStrukturFilnavn() {
        return "aap/aap_ordinaer.xml";
    }

    @Override
    public String getBundleName() {
        return "soknadaap";
    }

    @Override
    public List<String> getSoknadBolker(WebSoknad soknad) {
        return Arrays.asList(BOLK_PERSONALIA, BOLK_BARN);
    }

    @Override
    public boolean brukerNyOppsummering() {
        return true;
    }

    @Override
    public boolean skalSendeMedFullSoknad() {
        return true;
    }
}
