package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.springframework.stereotype.Component;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class HentFaktumValueHelper extends RegistryAwareHelper<String> {

    @Override
    public String getNavn() {
        return "hentFaktumValue";
    }

    @Override
    public String getBeskrivelse() {
        return "Returnerer verdien til et faktum tilhørende keyen som sendes inn";
    }

    @Override
    public CharSequence apply(String key, Options options) {
        WebSoknad soknad = finnWebSoknad(options.context);
        Faktum faktum = soknad.getFaktumMedKey(key);
        return faktum.getValue();
    }
}
