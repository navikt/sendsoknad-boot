package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

public interface HendelseRepository {

    void registrerOpprettetHendelse(WebSoknad soknad);

    void registrerHendelse(WebSoknad soknad, HendelseType hendelse);

    Integer hentVersjon(String behandlingsId);
}
