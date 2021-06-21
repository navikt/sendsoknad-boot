package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.LandOgPostInfoFetcherService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class HentPoststedHelper extends RegistryAwareHelper<String> {

    @Autowired
    private LandOgPostInfoFetcherService landOgPostInfoFetcherService;

    @Override
    public String getNavn() {
        return "hentPoststed";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter poststed for et postnummer fra kodeverk";
    }

    @Override
    public CharSequence apply(String postnummer, Options options) {
        return landOgPostInfoFetcherService.getPoststed(postnummer);
    }
}
