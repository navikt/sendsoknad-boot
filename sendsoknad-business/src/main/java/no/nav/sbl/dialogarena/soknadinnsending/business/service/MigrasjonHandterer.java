package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon.Migrasjon;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class MigrasjonHandterer {
    private static final Logger logger = getLogger(MigrasjonHandterer.class);

    private final HendelseRepository hendelseRepository;

    List<Migrasjon> migrasjoner = new ArrayList<>();

    @Autowired
    public MigrasjonHandterer(HendelseRepository hendelseRepository) {
        super();
        this.hendelseRepository = hendelseRepository;
    }

    public WebSoknad handterMigrasjon(WebSoknad soknad){
        WebSoknad migrertSoknad = soknad;

        if (migrasjoner == null || migrasjoner.size() <= 0) {
            logger.info("No migrations found!");
            return soknad;
        }

        Optional<Migrasjon> migrasjon = hentMigrasjonForSkjemanummerOgVersjon(migrertSoknad.getVersjon(), migrertSoknad.getskjemaNummer());

        logger.info("Migrasjon isPresent: {}", migrasjon.isPresent());
        if (migrasjon.isPresent()) {
            migrertSoknad = migrasjon.get().migrer(migrertSoknad.getVersjon(), migrertSoknad);

            hendelseRepository.registrerMigrertHendelse(migrertSoknad);

         //   Event metrikk = MetricsFactory.createEvent("sendsoknad.skjemamigrasjon");

            KravdialogInformasjon kravdialogInformasjon = KravdialogInformasjonHolder.hentKonfigurasjon(migrertSoknad.getskjemaNummer());
            String soknadTypePrefix = kravdialogInformasjon.getSoknadTypePrefix();

           // metrikk.addTagToReport("soknadstype", soknadTypePrefix);
           // metrikk.addTagToReport("skjemaversjon", String.valueOf(migrasjon.get().getTilVersjon()));

        //    metrikk.report();
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
