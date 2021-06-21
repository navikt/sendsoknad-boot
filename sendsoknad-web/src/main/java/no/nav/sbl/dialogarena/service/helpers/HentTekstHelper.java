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

import static org.apache.commons.lang3.LocaleUtils.toLocale;

@Component
public class HentTekstHelper extends RegistryAwareHelper<String> {

    @Autowired
    private CmsTekst cmsTekst;

    @Override
    public String getNavn() {
        return "hentTekst";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter tekst fra cms, prøver med søknadens prefix + key, før den prøver med bare keyen. Kan sende inn parametere.";
    }

    @Override
    public CharSequence apply(String key, Options options) {
        WebSoknad soknad = HandlebarsUtils.finnWebSoknad(options.context);
        Faktum sprakFaktum = soknad.getFaktumMedKey("skjema.sprak");
        String sprak = sprakFaktum == null ? "nb_NO" : sprakFaktum.getValue();
        final String bundleName = KravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getBundleName();

        String tekst = this.cmsTekst.getCmsTekst(key, options.params, soknad.getSoknadPrefix(), bundleName, toLocale(sprak));
        return tekst != null ? tekst : "";
    }
}
