package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;

import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonRequest;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


import static org.slf4j.LoggerFactory.getLogger;

@Service
public class EpostService {

    private static final Logger logger = getLogger(EpostService.class);

    //@Inject
    //@Named("dkifService")
    private DigitalKontaktinformasjonV1 dkif;
    
    
    @Autowired
    public EpostService(@Qualifier("dkifService") DigitalKontaktinformasjonV1 dkif) {
		super();
		this.dkif = dkif;
	}

	@Cacheable("dkifCache")
    public WSHentDigitalKontaktinformasjonResponse hentInfoFraDKIF(String ident) {
        try {
            return dkif.hentDigitalKontaktinformasjon(makeDKIFRequest(ident));
        }catch (HentDigitalKontaktinformasjonSikkerhetsbegrensing | HentDigitalKontaktinformasjonPersonIkkeFunnet e) {
            logger.error("Person ikke tilgjengelig i dkif: {}", e.getMessage());
        } catch (HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet e) {
            logger.info("Kunne ikke hente kontaktinformasjon fra dkif: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Hent info fra DKIF feiler med",e);
            logger.info("Feil ved henting fra dkif: {}", e.getMessage());
        }

        return new WSHentDigitalKontaktinformasjonResponse().withDigitalKontaktinformasjon(new WSKontaktinformasjon());
    }

    private WSHentDigitalKontaktinformasjonRequest makeDKIFRequest(String ident) {
        return new WSHentDigitalKontaktinformasjonRequest().withPersonident(ident);
    }
}
