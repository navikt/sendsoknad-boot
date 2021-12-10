package no.nav.sbl.dialogarena.utils;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.tokensupport.TokenUtils;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;

public class InnloggetBruker {

    @Autowired
    private PersonaliaBolk personaliaBolk;

    private static final Logger logger = getLogger(InnloggetBruker.class);

    public Personalia hentPersonalia() {
        String fnr = TokenUtils.getSubject();
        Personalia personalia = null;
        try {
            personalia = personaliaBolk.hentPersonalia(fnr);
        } catch (Exception e) {
            logger.error("Kunne ikke hente personalia");
        }
        return personalia;
    }
}
