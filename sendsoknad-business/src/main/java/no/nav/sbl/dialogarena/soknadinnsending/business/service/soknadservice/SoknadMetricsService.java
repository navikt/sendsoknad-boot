package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class SoknadMetricsService {

    private static final Logger logger = getLogger(SoknadMetricsService.class);

    private static final long RAPPORTERINGS_RATE = 15 * 60 * 1000; // hvert kvarter

    private MetricsEventFactory metricsEventFactory;

    private SoknadRepository lokalDb;
    
    
    @Autowired
    public SoknadMetricsService(MetricsEventFactory metricsEventFactory,@Qualifier("soknadInnsendingRepository") SoknadRepository lokalDb) {
		super();
		this.metricsEventFactory = metricsEventFactory;
		this.lokalDb = lokalDb;
	}

	public void startetSoknad(String skjemanummer, boolean erEttersending) {
        rapporterSoknad("soknad.start", skjemanummer, erEttersending);
    }

    public void sendtSoknad(String skjemanummer, boolean erEttersending) {
        rapporterSoknad("soknad.send", skjemanummer, erEttersending);
    }

    public void avbruttSoknad(String skjemanummer, boolean erEttersending) {
        rapporterSoknad("soknad.avbrutt", skjemanummer, erEttersending);
    }

    private void rapporterSoknad(String name, String skjemanummer, boolean erEttersending) {
        String soknadstype = getSoknadstype(skjemanummer, erEttersending);

    //    Event event = metricsEventFactory.createEvent(name);
    //    event.addFieldToReport("soknadstype", soknadstype);
    //    event.report();
    }

    private String getSoknadstype(String skjemanummer, boolean erEttersending) {
        String type = KravdialogInformasjonHolder.hentKonfigurasjon(skjemanummer).getSoknadTypePrefix();
        return (erEttersending ? "ettersending." : "") + type;
    }

    @Scheduled(fixedRate = RAPPORTERINGS_RATE)
    public void rapporterSoknadDatabaseStatus() {
        logger.info("Henter databasestatus for å rapportere metrics");
        Map<String, Integer> statuser = lokalDb.hentDatabaseStatus();

        for (Map.Entry<String, Integer> entry : statuser.entrySet()) {
            logger.info("Databasestatus for {} er {}", entry.getKey(), entry.getValue());
        //    Event event = metricsEventFactory.createEvent("status.database." + entry.getKey());
        //    event.addFieldToReport("antall", entry.getValue());
        //    event.report();
        }
    }
}
