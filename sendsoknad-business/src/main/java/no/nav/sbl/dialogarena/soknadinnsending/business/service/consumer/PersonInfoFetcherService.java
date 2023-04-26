package no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoService;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PersonInfoFetcherService {

    private static final Logger logger = getLogger(PersonInfoFetcherService.class);


    private PersonInfoService personInfoService;
    
    
    @Autowired
    public PersonInfoFetcherService(PersonInfoService personInfoService) {
		super();
		this.personInfoService = personInfoService;
	}



	public String hentYtelseStatus(String fnr) {
        String ytelsesstatus = personInfoService.hentYtelseStatus(fnr);
        logger.debug("Hentet ytelsesstatus="+ytelsesstatus);
        return ytelsesstatus;
    }
}
