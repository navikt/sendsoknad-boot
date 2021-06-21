package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.service.CmsTekst;
import no.nav.sbl.dialogarena.service.HandlebarsUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

import static org.apache.commons.lang3.LocaleUtils.toLocale;

@Component
public class HvisTekstFinnesHelper extends RegistryAwareHelper<String> {

    @Autowired
    private CmsTekst cmsTekst;

    @Override
    public String getNavn() {
        return "hvisTekstFinnes";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter tekst fra cms, prøver med søknadens prefix + key, før den prøver med bare keyen. Kan sende inn parametere.";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        String tekst = getCmsTekst(cmsTekst, key, options);

        if (tekst != null) {
            return options.fn();
        } else {
            return options.inverse();
        }
    }

    static String getCmsTekst(CmsTekst cmsTekst, String key, Options options) {
        WebSoknad soknad = HandlebarsUtils.finnWebSoknad(options.context);
        String soknadPrefix = soknad.getSoknadPrefix();
        final String bundleName = KravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getBundleName();
        Faktum sprakFaktum = soknad.getFaktumMedKey("skjema.sprak");
        String sprak = sprakFaktum == null ? "nb_NO" : sprakFaktum.getValue();

        return cmsTekst.getCmsTekst(key, new Object[0], soknadPrefix, bundleName, toLocale(sprak));
    }
}
