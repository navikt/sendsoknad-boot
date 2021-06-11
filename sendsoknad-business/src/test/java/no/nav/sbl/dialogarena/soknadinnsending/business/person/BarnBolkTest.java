package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class BarnBolkTest {
    private static final String BARN_IDENT = "10108000398"; //Ikke ekteperson
    private static final String BARN_FORNAVN = "Bjarne";
    private static final String BARN_ETTERNAVN = "Barnet";

    private static final String BARN2_IDENT = "01010081336"; //Ikke ekteperson
    private static final String BARN2_FORNAVN = "Per";
    private static final String BARN2_ETTERNAVN = "Barnet";
    private static final Long SOKNAD_ID = 21L;

    @InjectMocks
    private BarnBolk barnBolk;

    @Mock
    private PersonService personMock;

    private List<Barn> barn;

    @Before
    public void setup() {
        barn = new ArrayList<>();
        when(personMock.hentBarn(anyString())).thenReturn(barn);
    }

    @Test
    public void skalMappeBarnTilFaktum() {
        barn.add(lagBarn(BARN_IDENT, BARN_FORNAVN, BARN_ETTERNAVN));
        barn.add(lagBarn(BARN2_IDENT, BARN2_FORNAVN, BARN2_ETTERNAVN));
        List<Faktum> faktums = barnBolk.genererSystemFakta(BARN_IDENT, SOKNAD_ID);
        assertEquals(2, faktums.size());
        assertEquals(BARN_IDENT, faktums.get(0).getProperties().get("fnr"));
        assertEquals(BARN2_IDENT, faktums.get(1).getProperties().get("fnr"));
    }

    private Barn lagBarn(String ident, String fornavn, String etternavn) {
        return new Barn(SOKNAD_ID, null, "", ident, fornavn, "", etternavn);
    }
}
