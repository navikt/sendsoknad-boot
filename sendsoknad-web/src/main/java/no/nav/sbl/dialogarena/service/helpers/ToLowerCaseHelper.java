package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

@Component
public class ToLowerCaseHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "toLowerCase";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Gjør om en tekst til kun små bokstaver";
    }

    @Override
    public CharSequence apply(Object value, Options options) {
        return value != null? value.toString().toLowerCase(): "";
    }
}
