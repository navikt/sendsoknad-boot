package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tiltakspenger;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import org.joda.time.DateTime;

import java.util.List;

public class JsonTiltakspengerSoknad {


    private Long soknadId;


    private String soknadsType;
    private String skjemaNummer;
    private Integer versjon;
    private String uuid;
    private String brukerBehandlingId;
    private String behandlingskjedeId;
    private List<JsonTiltakspengerFaktum> fakta;
    private SoknadInnsendingStatus status;
    private String aktoerId;
    private DateTime opprettetDato;
    private DateTime sistLagret;
    private DelstegStatus delstegStatus;
    private List<VedleggJson> vedlegg;
    private String journalforendeEnhet;
    private String soknadPrefix;
    private String soknadUrl;
    private String fortsettSoknadUrl;
    private boolean erEttersending;

    public JsonTiltakspengerSoknad medSoknadsType(String soknadsType) {
        this.soknadsType = soknadsType;
        return this;
    }


    public JsonTiltakspengerSoknad medSoknadId(Long soknadId) {
        this.soknadId = soknadId;
        return this;
    }

    public JsonTiltakspengerSoknad medSkjemaNummer(String skjemaNummer) {
        this.skjemaNummer = skjemaNummer;
        return this;
    }

    JsonTiltakspengerSoknad medVersjon(Integer versjon) {
        this.versjon = versjon;
        return this;
    }

    public JsonTiltakspengerSoknad medUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    JsonTiltakspengerSoknad medBrukerBehandlingId(String brukerBehandlingId) {
        this.brukerBehandlingId = brukerBehandlingId;
        return this;
    }

    JsonTiltakspengerSoknad medBehandlingskjedeId(String behandlingskjedeId) {
        this.behandlingskjedeId = behandlingskjedeId;
        return this;
    }

    JsonTiltakspengerSoknad medFakta(List<JsonTiltakspengerFaktum> jsonDagpengerFaktums) {
        this.fakta = jsonDagpengerFaktums;
        return this;
    }

    JsonTiltakspengerSoknad medStatus(SoknadInnsendingStatus status) {
        this.status = status;
        return this;
    }

    JsonTiltakspengerSoknad medAktoerId(String aktoerId) {
        this.aktoerId = aktoerId;
        return this;
    }

    JsonTiltakspengerSoknad medOpprettetDato(DateTime opprettetDato) {
        this.opprettetDato = opprettetDato;
        return this;
    }


    JsonTiltakspengerSoknad medSistLagret(DateTime sistLagret) {
        this.sistLagret = sistLagret;
        return this;
    }

    JsonTiltakspengerSoknad medDelstegStatus(DelstegStatus delstegStatus) {
        this.delstegStatus = delstegStatus;
        return this;
    }

    JsonTiltakspengerSoknad medJournalforendeEnhet(String journalforendeEnhet) {
        this.journalforendeEnhet = journalforendeEnhet;
        return this;
    }


    public JsonTiltakspengerSoknad medVedlegg(List<VedleggJson> vedlegg) {
        this.vedlegg = vedlegg;
        return this;
    }

    public JsonTiltakspengerSoknad medSoknadPrefix(String soknadPrefix) {
        this.soknadPrefix = soknadPrefix;
        return this;
    }

    JsonTiltakspengerSoknad medSoknadUrl(String soknadUrl) {
        this.soknadUrl = soknadUrl;
        return this;
    }

    JsonTiltakspengerSoknad medFortsettSoknadUrl(String fortsettSoknadUrl) {
        this.fortsettSoknadUrl = fortsettSoknadUrl;
        return this;
    }

    JsonTiltakspengerSoknad medErEttersending(boolean erEttersending) {
        this.erEttersending = erEttersending;
        return this;
    }

    public Long getSoknadId() {
        return soknadId;
    }

    public String getSkjemaNummer() {
        return skjemaNummer;
    }

    public Integer getVersjon() {
        return versjon;
    }

    public String getUuid() {
        return uuid;
    }

    public String getBrukerBehandlingId() {
        return brukerBehandlingId;
    }

    public String getBehandlingskjedeId() {
        return behandlingskjedeId;
    }

    public List<JsonTiltakspengerFaktum> getFakta() {
        return fakta;
    }

    public SoknadInnsendingStatus getStatus() {
        return status;
    }

    public String getAktoerId() {
        return aktoerId;
    }

    public DateTime getOpprettetDato() {
        return opprettetDato;
    }

    public DateTime getSistLagret() {
        return sistLagret;
    }

    public DelstegStatus getDelstegStatus() {
        return delstegStatus;
    }

    public List<VedleggJson> getVedlegg() {
        return vedlegg;
    }

    public String getJournalforendeEnhet() {
        return journalforendeEnhet;
    }

    public String getSoknadPrefix() {
        return soknadPrefix;
    }

    public String getSoknadUrl() {
        return soknadUrl;
    }

    public String getFortsettSoknadUrl() {
        return fortsettSoknadUrl;
    }

    public String getSoknadsType() {
        return soknadsType;
    }

    public boolean getErEttersending() {
        return erEttersending;
    }
}
