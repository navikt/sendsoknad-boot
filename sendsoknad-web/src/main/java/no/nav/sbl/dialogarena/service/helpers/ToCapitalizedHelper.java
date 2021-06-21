package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Component;

@Component
public class ToCapitalizedHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "toCapitalized";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Gjør om en tekst til at alle ord starter med store bokstaver";
    }

    @Override
    public CharSequence apply(Object value, Options options) {
        if (value == null) {
            return "";
        }
        return WordUtils.capitalizeFully(value.toString());
    }
}
