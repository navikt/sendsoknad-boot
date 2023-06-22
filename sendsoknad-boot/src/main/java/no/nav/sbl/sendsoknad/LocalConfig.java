
package no.nav.sbl.sendsoknad;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Profile("local")
@Component
public class LocalConfig {

    // Setter properties for mocking når det kjøres lokalt
    @PostConstruct
    public void setProperty() {
        System.setProperty("tillatmock", "true");

        System.setProperty("start.kodeverk.withmock", "true");
        System.setProperty("start.sakogaktivitet.withmock", "true");
        System.setProperty("start.maalgruppe.withmock", "true");
        System.setProperty("start.personinfo.withmock", "true");
        System.setProperty("start.person.withmock", "true");
        System.setProperty("start.brukerprofil.withmock", "true");
        System.setProperty("start.arbeid.withmock", "true");
        System.setProperty("start.dkif.withmock", "true");
    }

}