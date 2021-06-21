package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.service.HandlebarsUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class FormaterLangDatoHelper extends RegistryAwareHelper<String> {

    @Override
    public String getNavn() {
        return "formaterLangDato";
    }

    @Override
    public String getBeskrivelse() {
        return "Gjør en datostreng om til langt, norsk format. F. eks. '17. januar 2015'";
    }

    @Override
    public CharSequence apply(String dato, Options options) {
        if (StringUtils.isNotEmpty(dato)) {
            WebSoknad soknad = HandlebarsUtils.finnWebSoknad(options.context);
            DateTimeFormatter langDatoformat = DateTimeFormat.forPattern("d. MMMM yyyy").withLocale(soknad.getSprak());
            return langDatoformat.print(DateTime.parse(dato));
        }
        return "";
    }
}
