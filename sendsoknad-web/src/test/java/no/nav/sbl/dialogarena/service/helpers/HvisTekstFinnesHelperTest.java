package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.service.CmsTekst;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HvisTekstFinnesHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HvisTekstFinnesHelper hvisTekstFinnesHelper;
    @Mock
    CmsTekst cmsTekst;


    @Before
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(hvisTekstFinnesHelper.getNavn(), hvisTekstFinnesHelper);
    }

    @Test
    public void trueOmTekstFinnes() throws IOException {
        when(cmsTekst.getCmsTekst(anyString(), any(Object[].class), anyString(), anyString(), any(Locale.class))).thenReturn("hei hei");
        WebSoknad webSoknad = new WebSoknad().medSoknadPrefix("mittprefix").medskjemaNummer("NAV 11-12.12");
        String compiled = handlebars.compileInline("{{#hvisTekstFinnes \"test\"}}true{{else}}false{{/hvisTekstFinnes}}").apply(webSoknad);
        assertThat(compiled).isEqualTo("true");
    }

    @Test
    public void falseOmTekstIkkeFinnes() throws IOException {
        when(cmsTekst.getCmsTekst(anyString(), any(Object[].class), anyString(), anyString(), any(Locale.class))).thenReturn(null);
        WebSoknad webSoknad = new WebSoknad().medSoknadPrefix("mittprefix").medskjemaNummer("NAV 11-12.12");
        String compiled = handlebars.compileInline("{{#hvisTekstFinnes \"test\"}}true{{else}}false{{/hvisTekstFinnes}}").apply(webSoknad);
        assertThat(compiled).isEqualTo("false");
    }
}
