package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.LandOgPostInfoFetcherService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HentPoststedHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    private HentPoststedHelper hentTekstHelper;
    @Mock
    private LandOgPostInfoFetcherService landOgPostInfoFetcherService;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(hentTekstHelper.getNavn(), hentTekstHelper);
        when(landOgPostInfoFetcherService.getPoststed("2233")).thenReturn("VESTMARKA");
    }

    @Test
    public void finnerPoststed() throws IOException {
        String compiled = handlebars.compileInline("{{hentPoststed \"2233\"}}").apply(null);

        assertThat(compiled).isEqualTo("VESTMARKA");
    }
}
