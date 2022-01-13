package no.nav.sbl.dialogarena.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.context.annotation.Bean;

public class MetricsConfig {

  //  @Bean
  //  public TimerAspect timerAspect() {
  //      return new TimerAspect();
  //  }

    @Bean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}
