package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.Miljovariabler;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class HentMiljovariabelHelper extends RegistryAwareHelper<String> {
    @Autowired
    private Miljovariabler informasjonService;

    public static final String NAVN = "hentMiljovariabel";

    private static final Logger logger = getLogger(HentMiljovariabelHelper.class);

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Finner miljovariabel fra key";
    }

    @Override
    public CharSequence apply(String key, Options options) {
        logger.info("Henter miljovariabel for key: " + key);
        return informasjonService.hentMiljovariabler().get(key);
    }
}
