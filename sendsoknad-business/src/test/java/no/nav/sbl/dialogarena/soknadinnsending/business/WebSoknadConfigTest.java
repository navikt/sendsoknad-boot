package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.ArbeidsforholdBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BarnBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebSoknadConfigTest {

    private static final Long AAP_SOKNAD_ID = 123L;
    private static final String AAP_SKJEMANUMMER = "NAV 11-13.05";

    @Mock
    SoknadRepository repository;

    @InjectMocks
    WebSoknadConfig config;

    @Before
    public void setUp() throws IOException {
        SkjemaOppslagService.initializeFromOldResult();
        when(repository.hentSoknadType(AAP_SOKNAD_ID)).thenReturn(AAP_SKJEMANUMMER);
    }

    @Test
    public void soknadtypePrefix() {
        String prefix = config.getSoknadTypePrefix(AAP_SOKNAD_ID);
        assertThat(prefix).isEqualTo("aap.ordinaer");
    }

    @Test
    public void testAtRiktigeSoknadBolkerErInkludert() {
        BolkService personalia = new PersonaliaBolk(null);
        BolkService barn = new BarnBolk(null);
        BolkService arbeidsforhold = new ArbeidsforholdBolk(null,null);

        List<BolkService> bolker = WebSoknadConfig.getSoknadBolker(new WebSoknad().medskjemaNummer(AAP_SKJEMANUMMER), asList(personalia, barn, arbeidsforhold));
        assertThat(bolker.contains(personalia)).isTrue();
        assertThat(bolker.contains(barn)).isTrue();
        assertThat(bolker.contains(arbeidsforhold)).isFalse();
    }
}
