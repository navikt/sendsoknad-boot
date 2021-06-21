package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.springframework.stereotype.Component;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class HentSkjemanummerHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "hentSkjemanummer";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Setter inn søknadens skjemanummer";
    }

    @Override
    public CharSequence apply(Object context, Options options) {
        WebSoknad soknad = finnWebSoknad(options.context);
        return soknad.getskjemaNummer();
    }
}
