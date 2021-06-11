package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

public class FakeMigrasjon extends Migrasjon {

    public FakeMigrasjon() {
        super(2,"NAV 11-13.05");
    }

    @Override
    public WebSoknad migrer(Integer fraVersjon, WebSoknad soknad) {
        soknad.medDelstegStatus(DelstegStatus.UTFYLLING)
                .medVersjon(getTilVersjon());
        return soknad;
    }
}
