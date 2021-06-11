package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon.Migrasjon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MigrasjonHandterer {

    
    private HendelseRepository hendelseRepository;

	List<Migrasjon> migrasjoner = new ArrayList<Migrasjon>();
	
	@Autowired
	public MigrasjonHandterer(HendelseRepository hendelseRepository) {
		super();
		this.hendelseRepository = hendelseRepository;
	}

    public WebSoknad handterMigrasjon(WebSoknad soknad){
        WebSoknad migrertSoknad = soknad;

        if(migrasjoner == null || migrasjoner.size() <= 0) return soknad;

        Optional<Migrasjon> migrasjon = hentMigrasjonForSkjemanummerOgVersjon(migrertSoknad.getVersjon(), migrertSoknad.getskjemaNummer());

        if(migrasjon.isPresent()){
            migrertSoknad = migrasjon.get().migrer(migrertSoknad.getVersjon(), migrertSoknad);

            hendelseRepository.registrerMigrertHendelse(migrertSoknad);

            Event metrikk = MetricsFactory.createEvent("sendsoknad.skjemamigrasjon");
            String soknadTypePrefix;

            KravdialogInformasjon kravdialogInformasjon = KravdialogInformasjonHolder.hentKonfigurasjon(migrertSoknad.getskjemaNummer());
            soknadTypePrefix = kravdialogInformasjon.getSoknadTypePrefix();

            metrikk.addTagToReport("soknadstype", soknadTypePrefix);
            metrikk.addTagToReport("skjemaversjon", String.valueOf(migrasjon.get().getTilVersjon()));

            metrikk.report();
        }

        return migrertSoknad;
    }

    

    private Optional<Migrasjon> hentMigrasjonForSkjemanummerOgVersjon(Integer versjon, String skjemanummer) {
        return migrasjoner.stream()
                .filter(migrasjon -> migrasjon.getMigrasjonSkjemanummer().equalsIgnoreCase(skjemanummer))
                .filter(migrasjon -> migrasjon.skalMigrere(versjon, skjemanummer))
                .findFirst();
    }
}
