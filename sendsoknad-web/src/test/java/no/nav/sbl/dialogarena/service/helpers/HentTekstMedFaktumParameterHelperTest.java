package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.service.CmsTekst;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.apache.commons.lang3.LocaleUtils.toLocale;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HentTekstMedFaktumParameterHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    private HentTekstMedFaktumParameterHelper hentTekstMedFaktumParameterHelper;
    @Mock
    private CmsTekst cmsTekst;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(hentTekstMedFaktumParameterHelper.getNavn(), hentTekstMedFaktumParameterHelper);
    }

    @Test
    public void kallerCmsTekstMedFaktumValueSomParameter() throws IOException {
        WebSoknad webSoknad = new WebSoknad()
                .medskjemaNummer("NAV 11-12.12")
                .medSoknadPrefix("mittprefix")
                .medFaktum(new Faktum()
                        .medKey("faktum.key")
                        .medValue("faktumValue"));

        handlebars.compileInline("{{hentTekstMedFaktumParameter \"cms.key\" \"faktum.key\"}}").apply(webSoknad);

        verify(cmsTekst, times(1)).getCmsTekst("cms.key", new Object[]{"faktumValue"}, "mittprefix", "soknadtilleggsstonader", toLocale("nb_NO"));
    }
}
