package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Locale;
import java.util.Properties;

import static org.apache.commons.lang3.LocaleUtils.toLocale;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CmsTekstTest {

    @InjectMocks
    CmsTekst cmsTekst;

    @Mock
    TekstHenter tekstHenter;

    Properties properties = new Properties();

    @Before
    public void setup() {
        when(tekstHenter.getBundleFor(anyString(), any(Locale.class))).thenReturn(properties);
    }

    @Test
    public void kallerMessageSourceToGangerMedOgUtenPrefixNarKeyIkkeEksisterer() {
        properties.put("min.key", "jegFinnes");

        String tekst = this.cmsTekst.getCmsTekst("min.key", null, "prefix", "bundlename", toLocale("nb_NO"));
        assertThat(tekst).isEqualTo("jegFinnes");

        properties.put("prefikset.kul.key", "finnesOgså");
        String prefiksTekst = this.cmsTekst.getCmsTekst("kul.key", null, "prefikset", "bundlename", toLocale("nb_NO"));
        assertThat(prefiksTekst).isEqualTo("finnesOgså");

        properties.put("key.med.parameter", "{0} av {1}");
        Object[] paramtere = new Object[2];
        paramtere[0] = "tekst0";
        paramtere[1] = "tekst1";
        String paramterTekst = this.cmsTekst.getCmsTekst("key.med.parameter", paramtere, "prefikset", "bundlename", toLocale("nb_NO"));
        assertThat(paramterTekst).isEqualTo("tekst0 av tekst1");
    }

    @Test
    public void getCmsTekstReturnererNullNarKeyMangler() {
        String tekst = cmsTekst.getCmsTekst("min.key", null, "prefix", "bundlename", toLocale("nb_NO"));

        assertThat(tekst).isEqualTo(null);
    }
}
