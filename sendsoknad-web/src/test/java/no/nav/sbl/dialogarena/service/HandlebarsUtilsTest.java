package no.nav.sbl.dialogarena.service;

import com.github.jknack.handlebars.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.service.oppsummering.OppsummeringsFaktum;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class HandlebarsUtilsTest {

    @Test
    public void testLagItererbarRespons() throws IOException {
        Handlebars handlebars = new Handlebars();

        Context context = Context.newContext(null);
        Template template = handlebars.compileInline("Faktum nr. {{index}}: {{value}} {{odd}}{{even}} {{first}}{{last}}\n");
        Options options = new Options.Builder(handlebars, "dummyHelper", TagType.SECTION, context, template).build();

        ArrayList<Faktum> strings = new ArrayList<>();
        strings.add(new Faktum().medValue("Faktum1"));
        strings.add(new Faktum().medValue("Faktum2"));
        strings.add(new Faktum().medValue("Faktum3"));

        String generated = HandlebarsUtils.lagItererbarRespons(options, strings);
        assertThat(generated).isEqualTo("Faktum nr. 0: Faktum1 even first\n" +
                "Faktum nr. 1: Faktum2 odd \n" +
                "Faktum nr. 2: Faktum3 even last\n");
    }

    @Test
    public void finnerWebSoknadIParentContext() {
        WebSoknad webSoknad = new WebSoknad();
        webSoknad.setSkjemaNummer("123456");

        Context parentContext = Context.newContext(webSoknad);
        Context middleContext = Context.newContext(parentContext, "noe");
        Context childContext = Context.newContext(middleContext, "noe annet");

        WebSoknad funnetWebSoknad = HandlebarsUtils.finnWebSoknad(childContext);

        assertThat(funnetWebSoknad.getskjemaNummer()).isEqualTo(webSoknad.getskjemaNummer());
    }

    @Test
    public void returnererNullOmWebSoknadIkkeIParentContext() {
        Context parentContext = Context.newContext("noe");
        Context childContext = Context.newContext(parentContext, "noe annet");

        WebSoknad funnetWebSoknad = HandlebarsUtils.finnWebSoknad(childContext);

        assertThat(funnetWebSoknad).isNull();
    }



    @Test
    public void finnerFaktumIParentContext() {
        Faktum faktum = new Faktum().medFaktumId(1234L);

        Context parentContext = Context.newContext(faktum);
        Context middleContext = Context.newContext(parentContext, "noe");
        Context childContext = Context.newContext(middleContext, "noe annet");

        Faktum funnetFaktum = HandlebarsUtils.finnFaktum(childContext);

        assertThat(funnetFaktum.getFaktumId()).isEqualTo(faktum.getFaktumId());
    }

    @Test
    public void returnererNullOmFaktumIkkeIParentContext() {
        Context parentContext = Context.newContext("noe");
        Context childContext = Context.newContext(parentContext, "noe annet");

        Faktum funnetFaktum = HandlebarsUtils.finnFaktum(childContext);

        assertThat(funnetFaktum).isNull();
    }

    @Test
    public void finnerOppsummeringsFaktumetSomLiggerPaaNaermesteContext() {
        OppsummeringsFaktum faktum = mock(OppsummeringsFaktum.class);

        Context parentContext = Context.newContext(faktum);
        Context middleContext = Context.newContext(parentContext, "middle");
        Context childContext = Context.newContext(middleContext, "child");

        OppsummeringsFaktum funnetFaktum = HandlebarsUtils.getOppsummeringsFaktum(childContext);

        assertThat(funnetFaktum).isEqualTo(faktum);
    }

    @Test
    public void returnerNullOmDetIkkeErNoenOppsummeringsfaktumPaaNoenParentContext() {
        Context parentContext = Context.newContext("noe");
        Context childContext = Context.newContext(parentContext, "noe annet");

        OppsummeringsFaktum funnetFaktum = HandlebarsUtils.getOppsummeringsFaktum(childContext);

        assertThat(funnetFaktum).isNull();
    }
}
