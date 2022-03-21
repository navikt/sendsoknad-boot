package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.NavFodselsnummer;
import no.nav.sbl.dialogarena.sendsoknad.domain.PersonAlder;

import org.springframework.stereotype.Component;


@Component
public class FnrTilKortDatoHelper extends RegistryAwareHelper<Object> {
    public static final String NAVN = "fnrTilKortDato";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Formatterer et gyldig fødselnummer til dato på formatet dd.mm.aaaa";
    }

    @Override
    public CharSequence apply(Object value, Options options) {
    
        NavFodselsnummer fnr2 = new NavFodselsnummer(value.toString());
        return fnr2.getDayInMonth() + "." + PersonAlder.parseMonth(fnr2.getMonth()) + "." + fnr2.getBirthYear();
    }
}
