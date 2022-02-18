package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.tjenester.AktiviteterMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class SakOgAktivitetWSConfig {
    public static final String SAKOGAKTIVITET_KEY = "start.sakogaktivitet.withmock";
    @Value("${soknad.webservice.arena.sakogaktivitet.url}")
    private String sakOgAktivitetEndpoint;

    @Bean
    public SakOgAktivitetV1 sakOgAktivitetEndpoint() {
        SakOgAktivitetV1 mock = sakOgAktivitetEndpointMock();
        SakOgAktivitetV1 prod = sakOgAktivitetEndpointWS();
        return createMetricsProxyWithInstanceSwitcher("SakOgAktivitet", prod, mock, SAKOGAKTIVITET_KEY, SakOgAktivitetV1.class);
    }

    public ServiceBuilder<SakOgAktivitetV1>.PortTypeBuilder<SakOgAktivitetV1> factory() {
        return new ServiceBuilder<>(SakOgAktivitetV1.class)
                .asStandardService()
                .withAddress(sakOgAktivitetEndpoint)
                .withWsdl("classpath:/wsdl/no/nav/tjeneste/virksomhet/sakOgAktivitet/v1/Binding.wsdl")
                .withServiceName(new QName("http://nav.no/tjeneste/virksomhet/sakOgAktivitet/v1/Binding", "SakOgAktivitet_v1"))
                .withEndpointName(new QName("http://nav.no/tjeneste/virksomhet/sakOgAktivitet/v1/Binding", "sakOgAktivitet_v1Port"))
                .build()
                .withHttpsMock()
                .withMDC()
                .withApiKey(FssProxyApiKey.value);
    }

    @Bean
    public SakOgAktivitetV1 sakOgAktivitetEndpointMock() {
        return new AktiviteterMock();
    }

    @Bean
    public SakOgAktivitetV1 sakOgAktivitetEndpointWS() {
        return factory().withUserSecurity().get();
    }

    @Bean
    public SakOgAktivitetV1 sakOgAktivitetSelftestEndpoint() {
        return factory().withSystemSecurity().get();
    }

    @Bean
    Pingable sakOgAktivitetPing() {
        return new Pingable() {
            @Override
            public Ping ping() {
                PingMetadata metadata = new PingMetadata(sakOgAktivitetEndpoint,"Sak Og Aktivitet v1", false);
                try {
                    sakOgAktivitetSelftestEndpoint().ping();
                    return lyktes(metadata);
                } catch (Exception e) {
                    return feilet(metadata, e);
                }
            }
        };
    }
}
