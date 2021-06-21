package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class HvisHarIkkeInnsendteDokumenterHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "hvisHarIkkeInnsendteDokumenter";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Sjekker om søknaden har ikke-innsendte vedlegg";
    }

    @Override
    public CharSequence apply(Object o, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        List<Vedlegg> vedlegg = soknad.getIkkeInnsendteVedlegg();
        if (vedlegg.isEmpty()) {
            return options.inverse(this);
        } else {
            return options.fn(this);
        }
    }
}
