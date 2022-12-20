package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;


import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.List;
import java.util.Optional;

public interface SoknadRepository {

    Long opprettSoknad(WebSoknad soknad);

    WebSoknad hentSoknad(Long id);
    WebSoknad hentSoknad(String behandlingsId);

    WebSoknad hentSoknadMedData(Long id);

    WebSoknad hentNyesteSoknadGittBehandlingskjedeId(String behandlingskjedeId);

    WebSoknad hentOpprinneligInnsendtSoknad(String behandlingskjedeId);

    WebSoknad hentSoknadMedVedlegg(String behandlingsId);

    List<Faktum> hentAlleBrukerData(String behandlingsId);

    // Only used by LagringsScheduler
    Optional<WebSoknad> plukkSoknadTilMellomlagring();
    // Only used by LagringsScheduler
    void leggTilbake(WebSoknad webSoknad);
    
    Long oppdaterFaktum(Faktum faktum);
    void oppdaterFaktumBatched(List<Faktum> faktum);
    Long opprettFaktum(long soknadId, Faktum faktum, Boolean systemFaktum);
    Long opprettFaktum(long soknadId, Faktum faktum);
    Long oppdaterFaktum(Faktum faktum, Boolean systemFaktum);

    void batchOpprettTommeFakta(List<Faktum> fakta);
    List<Long> hentLedigeFaktumIder(int antall);

    Faktum hentFaktum(Long faktumId);

    String hentBehandlingsIdTilFaktum(Long faktumId);

    List<Faktum> hentSystemFaktumList(Long soknadId, String key);

    void settSistLagretTidspunkt(Long soknadId);

    void slettBrukerFaktum(Long soknadId, Long faktumId);

    void slettGamleSoknader();

    void slettSoknad(WebSoknad soknad, HendelseType aarsakTilSletting);

    void oppdaterSoknadEtterInnsending(WebSoknad soknad);

    String hentSoknadType(Long soknadId);

    void settDelstegstatus(Long soknadId, DelstegStatus status);
    void settDelstegstatus(String behandlingsId, DelstegStatus status);

    void settJournalforendeEnhet(String behandlingsId, String journalforendeEnhet);

    void populerFraStruktur(WebSoknad soknad);

    Optional<WebSoknad> hentEttersendingMedBehandlingskjedeId(String behandlingsId);

    Faktum hentFaktumMedKey(Long soknadId, String faktumKey);
}
