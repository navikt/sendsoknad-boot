package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class SoknadMetricsService {

    private final MeterRegistry meterRegistry;

    @Autowired
    public SoknadMetricsService(MeterRegistry meterRegistry) {
        super();
        this.meterRegistry = meterRegistry;
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

    public void arkiveringsRespons(int antall) {
        meterRegistry.gauge("sendsoknad.ingen.arkiveringsrespons", antall);
    }

    public void arkiveringsFeil(int antall) {
        meterRegistry.gauge("sendsoknad.arkiveringsfeil", antall);
    }

    private void rapporterSoknad(String name, String skjemanummer, boolean erEttersending) {
        String soknadstype = getSoknadstype(skjemanummer, erEttersending);
        meterRegistry.counter(name, Collections.singletonList(Tag.of("soknadstype", soknadstype))).increment();
    }

    private String getSoknadstype(String skjemanummer, boolean erEttersending) {
        String type = KravdialogInformasjonHolder.hentKonfigurasjon(skjemanummer).getSoknadTypePrefix();
        return (erEttersending ? "ettersending." : "") + type;
    }
}