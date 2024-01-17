package no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PersonInfoFetcherService {

    private static final Logger logger = getLogger(PersonInfoFetcherService.class);

	public String hentYtelseStatus(String fnr) {
        String ytelsesstatus = "UKJENT";
        logger.info("Returnerer ytelsesstatus="+ytelsesstatus);
        return ytelsesstatus;
    }
}
