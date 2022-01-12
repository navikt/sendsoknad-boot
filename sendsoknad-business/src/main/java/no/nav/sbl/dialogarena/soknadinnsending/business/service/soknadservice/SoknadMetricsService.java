package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class SoknadMetricsService {

    private static final Logger logger = getLogger(SoknadMetricsService.class);

    private static final long RAPPORTERINGS_RATE = 15 * 60 * 1000; // hvert kvarter

    private final MeterRegistry meterRegistry;

    private final SoknadRepository lokalDb;


    @Autowired
    public SoknadMetricsService(MeterRegistry meterRegistry, @Qualifier("soknadInnsendingRepository") SoknadRepository lokalDb) {
		super();
		this.meterRegistry = meterRegistry;
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

        // For å bevare navnekonvensjonen som det tidligere benyttede NAV Metric biblioteket brukte.
        // Se: https://confluence.adeo.no/display/navnofor/Metrics+biblioteket
        name += ".event";

        meterRegistry.counter(name, List.of(Tag.of("soknadstype", soknadstype)));
    }

    private String getSoknadstype(String skjemanummer, boolean erEttersending) {
        String type = KravdialogInformasjonHolder.hentKonfigurasjon(skjemanummer).getSoknadTypePrefix();
        return (erEttersending ? "ettersending." : "") + type;
    }

    @Scheduled(fixedRate = RAPPORTERINGS_RATE)
    public void rapporterSoknadDatabaseStatus() {
        logger.debug("Henter databasestatus for å rapportere metrics");
        Map<String, Integer> statuser = lokalDb.hentDatabaseStatus();

        for (Map.Entry<String, Integer> entry : statuser.entrySet()) {
            logger.debug("Databasestatus for {} er {}", entry.getKey(), entry.getValue());
        //    Event event = metricsEventFactory.createEvent("status.database." + entry.getKey());
        //    event.addFieldToReport("antall", entry.getValue());
        //    event.report();
        }
    }
}
