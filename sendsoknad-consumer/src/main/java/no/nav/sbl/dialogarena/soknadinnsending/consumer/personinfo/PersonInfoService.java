package no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo;

import no.aetat.arena.fodselsnr.Fodselsnr;
import no.aetat.arena.personstatus.Personstatus;
import no.nav.arena.tjenester.person.v1.FaultGeneriskMsg;
import no.nav.arena.tjenester.person.v1.PersonInfoServiceSoap;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PersonInfoService {
    private static final Logger logger = getLogger(PersonInfoService.class);

    private PersonInfoServiceSoap personInfoEndpoint;

    static final String UKJENT = "UKJENT";
    static final String IKKE_REGISTRERT = "IKKE_REGISTRERT";
    
    
    @Autowired
    public PersonInfoService(PersonInfoServiceSoap personInfoEndpoint) {
		super();
		this.personInfoEndpoint = personInfoEndpoint;
	}

	public String hentYtelseStatus(String fnr) {
        try {
            return mapTilYtelsesstatus(hentPersontatus(fnr));
        } catch (Exception e) {
            logger.error("Feil ved henting av personstatus", e);
            return UKJENT;
        }
    }

    private Personstatus hentPersontatus(String fnr) throws FaultGeneriskMsg {
        return personInfoEndpoint.hentPersonStatus(new Fodselsnr().withFodselsnummer(fnr));
    }

    private static String mapTilYtelsesstatus(Personstatus personstatus) {
        if (personstatusErTom(personstatus)) {
            return IKKE_REGISTRERT;
        }
        return personstatus.getPersonData().getStatusYtelse();
    }

    private static boolean personstatusErTom(Personstatus personstatus) {
        return personstatus == null || personstatus.getPersonData() == null;
    }
}
