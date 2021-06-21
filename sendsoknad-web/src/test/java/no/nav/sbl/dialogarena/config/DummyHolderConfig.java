package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.EttersendingService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;

import javax.inject.Named;

@Configuration
public class DummyHolderConfig {

    @Bean
    @Named("soknadInnsendingRepository")
    public SoknadRepository soknadInnsendingRepository() {
        return null;
    }

    @Bean
    @Named("vedleggRepository")
    public VedleggRepository vedleggRepository() {
        return null;
    }

    @Bean
    public SoknadDataFletter soknadDataFletter() {
        return null;
    }

    @Bean
    public FaktaService faktaService() {
        return null;
    }

    @Bean
    public EttersendingService ettersendingService() {
        return null;
    }

    @Bean
    public WebSoknadConfig config() {
        return null;
    }

    @Bean
    public JavaMailSender mailSender() {
        return null;
    }

    @Bean
    @Named("threadPoolTaskExecutor")
    public TaskExecutor threadPoolTaskExecutor() {
        return null;
    }
}
