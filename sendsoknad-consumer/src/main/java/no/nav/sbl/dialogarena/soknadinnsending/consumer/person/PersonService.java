package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.IkkeFunnetException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.SikkerhetsBegrensningException;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.FamilierelasjonTransform.mapFamilierelasjon;
import static org.slf4j.LoggerFactory.getLogger;


@Component
public class PersonService {

    private static final Logger logger = getLogger(PersonService.class);

    //@Inject
    //@Named("personEndpoint")
    private PersonPortType personEndpoint;

    //@Inject
    //@Named("personSelftestEndpoint")
    private PersonPortType personSelftestEndpoint;
    
    
    @Autowired
    public PersonService(@Qualifier("personEndpoint") PersonPortType personEndpoint,@Qualifier("personSelftestEndpoint") PersonPortType personSelftestEndpoint) {
		super();
		this.personEndpoint = personEndpoint;
		this.personSelftestEndpoint = personSelftestEndpoint;
	}

	@Cacheable(value = "barnCache", key = "#fodselsnummer")
    public HentKjerneinformasjonResponse hentKjerneinformasjon(String fodselsnummer) {
        HentKjerneinformasjonRequest request = lagXMLRequestKjerneinformasjon(fodselsnummer);
        try {
            return personEndpoint.hentKjerneinformasjon(request);
        } catch (HentKjerneinformasjonPersonIkkeFunnet e) {
            logger.error("Fant ikke bruker i TPS (Person-servicen).", e);
            throw new IkkeFunnetException("fant ikke bruker: " + request.getIdent(), e);
        } catch (HentKjerneinformasjonSikkerhetsbegrensning e) {
            logger.warn("Kunne ikke hente bruker fra TPS (Person-servicen).", e);
            throw new SikkerhetsBegrensningException("Kunne ikke hente bruker: " + request.getIdent(), e);
        } catch (WebServiceException e) {
            throw new SendSoknadException("Kunne ikke hente bruker fra TPS (Person-servicen)", e);
        }
    }

    public List<Barn> hentBarn(String fodselsnummer) {
        try {
            return mapFamilierelasjon(hentKjerneinformasjon(fodselsnummer));
        } catch (IkkeFunnetException e) {
            logger.warn("Ikke funnet person i TPS");
        } catch (WebServiceException e) {
            logger.error("Ingen kontakt med TPS.", e);
        }
        return new ArrayList<>();
    }
    
    public void ping() {
        personSelftestEndpoint.ping();
    }

    private HentKjerneinformasjonRequest lagXMLRequestKjerneinformasjon(String fodselsnummer) {
        HentKjerneinformasjonRequest request = new HentKjerneinformasjonRequest();
        request.setIdent(fodselsnummer);
        return request;
    }

}
