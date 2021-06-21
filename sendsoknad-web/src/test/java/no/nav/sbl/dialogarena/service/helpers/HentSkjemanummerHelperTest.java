package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPOrdinaerInformasjon;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class HentSkjemanummerHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        HentSkjemanummerHelper helper = new HentSkjemanummerHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void viserSkjemanummer() throws IOException {
        WebSoknad webSoknad = new WebSoknad();
        webSoknad.setSkjemaNummer("123456");

        String compiled = handlebars.compileInline("Skjemanummer: {{ hentSkjemanummer }}").apply(webSoknad);
        assertThat(compiled).isEqualTo("Skjemanummer: 123456");
    }

    @Test
    public void viserSkjemanummerForAAP() throws IOException {
        String skjemaNummer = new AAPOrdinaerInformasjon().getSkjemanummer().get(0);
        WebSoknad webSoknad = new WebSoknad();
        webSoknad.setSkjemaNummer(skjemaNummer);

        String compiled = handlebars.compileInline("Skjemanummer: {{ hentSkjemanummer }}").apply(webSoknad);
        assertThat(compiled).isEqualTo("Skjemanummer: " + skjemaNummer);
    }

    @Test
    public void viserSkjemaNummerSelvOmWebSoknadIParentContext() throws IOException {
        WebSoknad webSoknad = new WebSoknad();
        webSoknad.setSkjemaNummer("999999");

        Context parentContext = Context.newContext(webSoknad);
        Context childContext = Context.newContext(parentContext, "555555");

        String compiled = handlebars.compileInline("Skjemanummer: {{ hentSkjemanummer }}").apply(childContext);
        assertThat(compiled).isEqualTo("Skjemanummer: 999999");
    }
}
