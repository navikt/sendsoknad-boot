package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.service.CmsTekst;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

import static no.nav.sbl.dialogarena.service.helpers.HvisTekstFinnesHelper.getCmsTekst;

@Component
public class VisCheckboxHelper extends RegistryAwareHelper<String> {
    @Autowired
    private CmsTekst cmsTekst;

    public static final String NAVN = "visCheckbox";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "hvis value er \"true\" eller key.false-teksten finnes";
    }

    @Override
    public CharSequence apply(String value, Options options) throws IOException {
        String key = options.param(0) + ".false";
        String tekst = getCmsTekst(cmsTekst, key, options);

        if (tekst != null || "true".equals(value)) {
            return options.fn();
        } else {
            return options.inverse();
        }
    }
}
