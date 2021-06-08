package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.arbeid.ArbeidsforholdMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class ArbeidWSConfig {

    public static final String ARBEID_KEY = "start.arbeid.withmock";

    @Value("${soknad.webservice.arbeid.arbeidsforhold.url}")
    private String arbeidsforholdEndpoint;

    private ServiceBuilder<ArbeidsforholdV3>.PortTypeBuilder<ArbeidsforholdV3> factory() {
        return new ServiceBuilder<>(ArbeidsforholdV3.class)
                .asStandardService()
                .withAddress(arbeidsforholdEndpoint)
                .withWsdl("classpath:/wsdl/no/nav/tjeneste/virksomhet/arbeidsforhold/v3/Binding.wsdl")
                .withServiceName(new QName("http://nav.no/tjeneste/virksomhet/arbeidsforhold/v3/Binding", "Arbeidsforhold_v3"))
                .withEndpointName(new QName("http://nav.no/tjeneste/virksomhet/arbeidsforhold/v3/Binding", "Arbeidsforhold_v3Port"))
                .build()
                .withHttpsMock()
                .withMDC();
    }

    @Bean
    public ArbeidsforholdV3 arbeidEndpoint() {
        ArbeidsforholdV3 mock = new ArbeidsforholdMock().arbeidMock();
        ArbeidsforholdV3 prod = factory().withUserSecurity().get();
        return createMetricsProxyWithInstanceSwitcher("Arbeidsforhold", prod, mock, ARBEID_KEY, ArbeidsforholdV3.class);
    }

    @Bean
    public ArbeidsforholdV3 arbeidSelftestEndpoint() {
        return factory().withSystemSecurity().get();
    }

    @Bean
    Pingable arbeidPing() {
        return new Pingable() {
            @Override
            public Ping ping() {
                PingMetadata metadata = new PingMetadata(arbeidsforholdEndpoint,"Arbeidsforhold_v3", false);
                try {
                    arbeidSelftestEndpoint().ping();
                    return lyktes(metadata);
                } catch (Exception e) {
                    return feilet(metadata, e);
                }
            }
        };
    }
}
