package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.arbeid.ArbeidsforholdMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class OrganisasjonWSConfig extends WSConfig {

    public static final String ARBEID_KEY = "start.arbeid.withmock";

    @Value("${soknad.webservice.arbeid.organisasjon.url}")
    private String organisasjonEndpoint;

    private ServiceBuilder<OrganisasjonV4>.PortTypeBuilder<OrganisasjonV4> factory() {
        return new ServiceBuilder<>(OrganisasjonV4.class)
                .asStandardService()
                .withAddress(organisasjonEndpoint)
                .withWsdl("classpath:/wsdl/no/nav/tjeneste/virksomhet/organisasjon/v4/Binding.wsdl")
                .withServiceName(new QName("http://nav.no/tjeneste/virksomhet/organisasjon/v4/Binding", "Organisasjon_v4"))
                .withEndpointName(new QName("http://nav.no/tjeneste/virksomhet/organisasjon/v4/Binding", "Organisasjon_v4Port"))
                .build()
                .withHttpsMock()
                .withMDC()
                .withApiKey(super.apiKey);
    }

    @Bean(name="organisasjonEndpoint")
    public OrganisasjonV4 organisasjonEndpoint() {
        OrganisasjonV4 mock = new ArbeidsforholdMock().organisasjonMock();
        OrganisasjonV4 prod = factory().withUserSecurity().get();
        return createMetricsProxyWithInstanceSwitcher("Organisasjon", prod, mock, ARBEID_KEY, OrganisasjonV4.class);
    }

    @Bean(name="organisasjonSelftestEndpoint")
    public OrganisasjonV4 organisasjonSelftestEndpoint() {
        return factory().withSystemSecurity().get();
    }

    @Bean
    Pingable organisasjonPing() {
        return new Pingable() {
            @Override
            public Ping ping() {
                PingMetadata metadata = new PingMetadata(organisasjonEndpoint,"Organisasjon v4 - Henter organisasjonsinfo for arbeidsforhold", false);
                try {
                    organisasjonSelftestEndpoint().ping();
                    return lyktes(metadata);
                } catch (Exception e) {
                    return feilet(metadata, e);
                }
            }
        };
    }
}
