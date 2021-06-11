package no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonInfoFetcherService {

    
    private PersonInfoService personInfoService;
    
    
    @Autowired
    public PersonInfoFetcherService(PersonInfoService personInfoService) {
		super();
		this.personInfoService = personInfoService;
	}



	public String hentYtelseStatus(String fnr) {
        return personInfoService.hentYtelseStatus(fnr);
    }
}
