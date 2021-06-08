package no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia;

import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adressetype;
import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import org.junit.Test;

import static org.junit.Assert.*;

public class PersonTest {
    @Test
    public void skalReturnereTrueForPostadresseUtland() {
        Personalia personalia = new Personalia();
        Adresse gjeldendeAdresse = new Adresse();
        gjeldendeAdresse.setAdressetype(Adressetype.POSTADRESSE_UTLAND.name());
        personalia.setGjeldendeAdresse(gjeldendeAdresse);

        assertTrue(personalia.harUtenlandskAdresse());
    }

    @Test
    public void skalReturnereTrueForFolkeregistrertPostadresseUtland() {
        Personalia personalia = new Personalia();
        Adresse gjeldendeAdresse = new Adresse();
        gjeldendeAdresse.setAdressetype(Adressetype.UTENLANDSK_ADRESSE.name());
        personalia.setGjeldendeAdresse(gjeldendeAdresse);

        assertTrue(personalia.harUtenlandskAdresse());
    }

    @Test
    public void skalReturnereTrueForMidlertidigPostadresseUtland() {
        Personalia personalia = new Personalia();
        Adresse gjeldendeAdresse = new Adresse();
        gjeldendeAdresse
                .setAdressetype(Adressetype.MIDLERTIDIG_POSTADRESSE_UTLAND
                        .name());
        personalia.setGjeldendeAdresse(gjeldendeAdresse);

        assertTrue(personalia.harUtenlandskAdresse());
    }

    @Test
    public void skalReturnereFalseForMidlertidigPostadresseNorge() {
        Personalia personalia = new Personalia();
        Adresse gjeldendeAdresse = new Adresse();
        gjeldendeAdresse
                .setAdressetype(Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE
                        .name());
        personalia.setGjeldendeAdresse(gjeldendeAdresse);

        assertFalse(personalia.harUtenlandskAdresse());
    }

    @Test
    public void skalReturnereFalseForFolkeregistrertAdresseNorge() {
        Personalia personalia = new Personalia();
        Adresse gjeldendeAdresse = new Adresse();
        gjeldendeAdresse.setAdressetype(Adressetype.BOSTEDSADRESSE.name());
        personalia.setGjeldendeAdresse(gjeldendeAdresse);

        assertFalse(personalia.harUtenlandskAdresse());
    }

    @Test
    public void skalReturneGuttVedGuttePersonNummer() {
        String dato = "060258";
        String individisfferEnOgTo = "00";
        String kjonnSiffer = "1";
        String kontrollsiffer = "74";

        String fnr = dato + individisfferEnOgTo + kjonnSiffer + kontrollsiffer;
        Barn barn = new Barn(1L, null, null, fnr, "", "", "svenskeby").withLand("Norge");

        assertEquals("m", barn.getKjonn());
        assertEquals("Norge", barn.getLand());
    }

    @Test
    public void skalReturneJenteVedJentePersonNummer() {
        String dato = "140571";
        String individisfferEnOgTo = "32";
        String kjonnSiffer = "8";
        String kontrollsiffer = "42";

        String fnr = dato + individisfferEnOgTo + kjonnSiffer + kontrollsiffer;
        Barn barn = new Barn(1L, null, null, fnr, "janne", "j", "jensen").withLand("Norge");

        assertEquals("k", barn.getKjonn());
    }
}
