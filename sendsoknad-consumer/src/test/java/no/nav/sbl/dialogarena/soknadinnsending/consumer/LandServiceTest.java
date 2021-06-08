package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.dto.Land;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.LandListe.EOS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class LandServiceTest {

    @Mock
    private Kodeverk kodeverk;

    @InjectMocks
    LandService service;

    @Test
    public void resultatetSkalInneholdeNorgeSelvOmNorgeIkkeKomFraKodeverk() {
        List<Land> land;

        land = service.hentLand(null);
        assertEquals(1, land.size());
        assertEquals("Norge", land.get(0).getText());

        land = service.hentLand(EOS);
        assertEquals(31, land.size());
        List<String> landNavn = land.stream().map(Land::getText).collect(Collectors.toList());
        assertTrue(landNavn.containsAll(asList("SWE", "ISL", "POL")));
    }
}
