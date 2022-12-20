package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.sendsoknad.domain.Steg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.XmlService;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static javax.xml.bind.JAXBContext.newInstance;

@Component
public class WebSoknadConfig {

    private static final Logger LOG = LoggerFactory.getLogger(WebSoknadConfig.class);

    private final SoknadRepository repository;


    @Autowired
    public WebSoknadConfig(@Qualifier("soknadInnsendingRepository") SoknadRepository repository) {
		this.repository = repository;
	}

	public String getSoknadTypePrefix(long soknadId) {
        KravdialogInformasjon skjemaConfig = finnSkjemaConfig(soknadId);
        return skjemaConfig.getSoknadTypePrefix();
    }

    public String getSoknadUrl(long soknadId) {
        KravdialogInformasjon skjemaConfig = finnSkjemaConfig(soknadId);
        return System.getProperty(skjemaConfig.getSoknadUrlKey());
    }

    public String getFortsettSoknadUrl(long soknadId) {
        KravdialogInformasjon skjemaConfig = finnSkjemaConfig(soknadId);
        return System.getProperty(skjemaConfig.getFortsettSoknadUrlKey());
    }

    public SoknadStruktur hentStruktur(String skjemaNummer) {
        KravdialogInformasjon skjemaConfig = KravdialogInformasjonHolder.hentKonfigurasjon(skjemaNummer);
        SoknadStruktur struktur = hentStrukturForSkjemanavn(skjemaConfig);
        //For å støtte ulikt tema på forskjellige skjema på samme konfigurasjon
        try {
            String tema = SkjemaOppslagService.getTema(skjemaNummer);
            struktur.setTemaKode(tema);
        } catch (Exception e) {
            LOG.warn("Fant ikke tema for skjema i kodeverk: " + e, e);
        }
        return struktur;
    }

    private SoknadStruktur hentStrukturForSkjemanavn(KravdialogInformasjon skjemaConfig) {
        String type = skjemaConfig.getStrukturFilnavn();
        if (type == null || type.isEmpty()) {
            throw new SendSoknadException("Fant ikke strukturdokument for skjema: " + skjemaConfig.getClass().getSimpleName());
        }

        try {
            StreamSource xmlSource = XmlService.lastXmlFil("soknader/" + type);

            Unmarshaller unmarshaller = newInstance(SoknadStruktur.class).createUnmarshaller();
            return unmarshaller.unmarshal(xmlSource, SoknadStruktur.class).getValue();

        } catch (JAXBException | IOException e) {
            throw new RuntimeException("Kunne ikke laste definisjoner. ", e);
        }
    }

    public static List<BolkService> getSoknadBolker(WebSoknad soknad, Collection<BolkService> alleBolker) {
        KravdialogInformasjon skjemaConfig = KravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer());
        List<String> configBolker = skjemaConfig.getSoknadBolker(soknad);

        List<BolkService> soknadBolker = new ArrayList<>();
        for (BolkService bolk : alleBolker) {
            if (configBolker.contains(bolk.tilbyrBolk())) {
                soknadBolker.add(bolk);
            }
        }
        return soknadBolker;
    }

    private KravdialogInformasjon finnSkjemaConfig(Long soknadId) {
        String skjemanummer = repository.hentSoknadType(soknadId);
        return KravdialogInformasjonHolder.hentKonfigurasjon(skjemanummer);
    }

    public Steg[] getStegliste(Long soknadId) {
        KravdialogInformasjon skjemaConfig = finnSkjemaConfig(soknadId);
        return skjemaConfig.getStegliste();
    }

    public boolean brukerNyOppsummering(Long soknadId) {
        KravdialogInformasjon skjemaConfig = finnSkjemaConfig(soknadId);
        return skjemaConfig.brukerNyOppsummering();
    }

    public boolean skalSendeMedFullSoknad(Long soknadId) {
        return finnSkjemaConfig(soknadId).skalSendeMedFullSoknad();
    }
}
