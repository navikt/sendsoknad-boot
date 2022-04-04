package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.IkkeFunnetException;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class FaktaService {
	
//	@Autowired
//	@Qualifier("soknadInnsendingRepository")
    private SoknadRepository repository;
//	@Autowired
//	@Qualifier("vedleggRepository")
    private VedleggRepository vedleggRepository;
    
    
    @Autowired
    public FaktaService(@Qualifier("soknadInnsendingRepository") SoknadRepository repository,@Qualifier("vedleggRepository") VedleggRepository vedleggRepository) {
		super();
		this.repository = repository;
		this.vedleggRepository = vedleggRepository;
	}
	

	private static final String EKSTRA_VEDLEGG_KEY = "ekstraVedlegg";
    private static final Logger logger = getLogger(FaktaService.class);
    private static final List<String> IGNORERTE_KEYS = Arrays.asList(EKSTRA_VEDLEGG_KEY, Personalia.EPOST_KEY, "skjema.sprak");

    public List<Faktum> hentFakta(String behandlingsId) {
        return repository.hentAlleBrukerData(behandlingsId);
    }

    public String hentBehandlingsId(Long faktumId) {
        return repository.hentBehandlingsIdTilFaktum(faktumId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Faktum opprettBrukerFaktum(String behandlingsId, Faktum faktum) {
        Long soknadId = repository.hentSoknad(behandlingsId).getSoknadId();
        faktum.setSoknadId(soknadId);
        faktum.setType(BRUKERREGISTRERT);
        Long faktumId = repository.opprettFaktum(soknadId, faktum);

        repository.settSistLagretTidspunkt(soknadId);

        if ( faktum.getKey() != null && faktumId != null) {
           settDelstegStatus(soknadId, faktum.getKey());
        }

        return repository.hentFaktum(faktumId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Faktum lagreBrukerFaktum(Faktum faktum) {
        Long soknadId = faktum.getSoknadId();
        faktum.setType(BRUKERREGISTRERT);

        Long faktumId = repository.oppdaterFaktum(faktum);
        repository.settSistLagretTidspunkt(soknadId);

        if ( faktum.getKey() != null && faktum.getFaktumId() != null) {
            settDelstegStatus(soknadId, faktum.getKey());
        }

        return repository.hentFaktum(faktumId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void lagreSystemFakta(final WebSoknad soknad, List<Faktum> fakta) {
        fakta.forEach(faktum->{
                Faktum existing;

                if (faktum.getUnikProperty() == null) {
                    existing = soknad.getFaktumMedKey(faktum.getKey());
                } else {
                    existing = soknad.getFaktaMedKeyOgProperty(faktum.getKey(), faktum.getUnikProperty(), faktum.getProperties().get(faktum.getUnikProperty()));
                }

                if (existing != null) {
                    faktum.setFaktumId(existing.getFaktumId());
                    faktum.kopierFaktumegenskaper(existing);
                }
                faktum.setType(SYSTEMREGISTRERT);
                if (faktum.getFaktumId() != null) {
                    repository.oppdaterFaktum(faktum, true);
                } else {
                    repository.opprettFaktum(soknad.getSoknadId(), faktum, true);
                }
            }
        );
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Long lagreSystemFaktum(Long soknadId, Faktum f) {
        logger.debug("*** Lagrer systemfaktum ***: " + f.getKey());
        f.setType(SYSTEMREGISTRERT);
        List<Faktum> fakta = repository.hentSystemFaktumList(soknadId, f.getKey());


        for (Faktum faktum : fakta) {
            if (faktum.getKey().equals(f.getKey())) {
                f.setFaktumId(faktum.getFaktumId());
                break;
            }
        }

        Long lagretFaktumId;
        if (f.getFaktumId() != null) {
            lagretFaktumId = repository.oppdaterFaktum(f, true);
        } else {
            lagretFaktumId = repository.opprettFaktum(soknadId, f, true);
        }

        repository.settSistLagretTidspunkt(soknadId);
        return lagretFaktumId;
    }


    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void slettBrukerFaktum(Long faktumId) {
        final Faktum faktum;
        try {
            faktum = repository.hentFaktum(faktumId);
        } catch (IncorrectResultSizeDataAccessException e) {
            logger.info("Skipped delete because faktum does not exist.");
            throw new IkkeFunnetException("Faktum ikke funnet", e, "faktum.exception.ikkefunnet");
        }
        Long soknadId = faktum.getSoknadId();

        String faktumKey = faktum.getKey();
        List<Vedlegg> vedleggliste = vedleggRepository.hentVedleggForFaktum(soknadId, faktumId);

        for (Vedlegg vedlegg : vedleggliste) {
            vedleggRepository.slettVedleggOgData(soknadId, vedlegg);
        }
        repository.slettBrukerFaktum(soknadId, faktumId);
        repository.settSistLagretTidspunkt(soknadId);

        if ( faktum.getKey() != null && faktum.getFaktumId() != null) {
            settDelstegStatus(soknadId, faktumKey);
        }
    }

    private void settDelstegStatus(Long soknadId, String faktumKey) {
        WebSoknad webSoknad = repository.hentSoknad(soknadId);

        //Sjekker og setter delstegstatus dersom et faktum blir lagret, med mindre det er visse keys
        if (!IGNORERTE_KEYS.contains(faktumKey)) {
            webSoknad.validerDelstegEndring(DelstegStatus.UTFYLLING);
            repository.settDelstegstatus(soknadId, DelstegStatus.UTFYLLING);
        }
    }

    public Faktum hentFaktumMedKey(Long soknadId, String key) {
        return repository.hentFaktumMedKey(soknadId, key);
    }

    public Faktum hentFaktum(Long faktumId) {
        return repository.hentFaktum(faktumId);
    }

    public void lagreFaktum(Long soknadId, Faktum faktum) {
        repository.opprettFaktum(soknadId, faktum);
    }
}
