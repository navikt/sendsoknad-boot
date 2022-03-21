package no.nav.sbl.dialogarena.sendsoknad.domain;

import org.joda.time.LocalDate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

import static org.joda.time.LocalDate.parse;
import static org.joda.time.Years.yearsBetween;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonAlder implements Serializable{

    private LocalDate fodselsdato;

    public PersonAlder(String dNummerEllerFodselsnummer){
        this.fodselsdato = parse(hentFodselsdatoFraFnr(dNummerEllerFodselsnummer));
    }

    public int getAlder() {
        return yearsBetween(fodselsdato, new LocalDate()).getYears();
    }

    private String hentFodselsdatoFraFnr(String fodselsnummer){
        NavFodselsnummer fnr = new NavFodselsnummer(fodselsnummer);
        return fnr.getBirthYear() + "-" + parseMonth(fnr.getMonth()) + "-" + fnr.getDayInMonth();
    }
    
    private static String parseMonth(String month) {
        char fChar = month.charAt(0);
        if (fChar=='8' || fChar =='9' ) {
            return fChar == '8' ? "0" + month.charAt(1) : "1" + month.charAt(1);
        }
        else {
            return month;
        }
    }

}

