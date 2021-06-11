package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

public class VedleggsgenereringUtilTest {

    public static final Long SOKNAD_ID = 1L;
    public static final Long FAKTUM_ID = 2L;
    public static final String SKJEMA_NUMMER = "Q7";
    public static final String TILLEGGSKEY = "tilleggskey";
    private Vedlegg vedlegg;

    @Before
    public void setUp() {
        vedlegg = new Vedlegg(SOKNAD_ID, FAKTUM_ID, SKJEMA_NUMMER, VedleggKreves);
        vedlegg.setVedleggId(3L);
    }

    @Test
    public void skalVaereUlikeVedleggVedUlikSoknadsid() {
        Vedlegg nyttVedlegg = new Vedlegg(11L, FAKTUM_ID, SKJEMA_NUMMER, VedleggKreves);
        nyttVedlegg.setVedleggId(4L);

        assertVedleggUlike(nyttVedlegg, vedlegg);
    }

    @Test
    public void skalVaereLikeVedleggSelvOmVedleggsidErUlik() {
        Vedlegg nyttVedlegg = new Vedlegg(SOKNAD_ID, FAKTUM_ID, SKJEMA_NUMMER, VedleggKreves);
        nyttVedlegg.setVedleggId(4L);

        assertVedleggLike(nyttVedlegg, vedlegg);
    }

    @Test
    public void skalVaereUlikeVedleggVedUlikSkjemanummer() {
        Vedlegg nyttVedlegg = new Vedlegg(SOKNAD_ID, FAKTUM_ID, "N3", VedleggKreves);
        nyttVedlegg.setVedleggId(4L);

        assertVedleggUlike(nyttVedlegg, vedlegg);
    }

    @Test
    public void skalVaereUliktVedleggVedForskjelligStatus() {
        Vedlegg nyttVedlegg = new Vedlegg(SOKNAD_ID, FAKTUM_ID, SKJEMA_NUMMER, IkkeVedlegg);
        nyttVedlegg.setVedleggId(4L);

        assertVedleggUlike(vedlegg, nyttVedlegg);
    }

    @Test
    public void skalVaereUlikeVedleggVedUlikFaktumid() {
        Vedlegg nyttVedlegg = new Vedlegg(SOKNAD_ID, 10L, SKJEMA_NUMMER, VedleggKreves);
        nyttVedlegg.setVedleggId(4L);

        assertVedleggUlike(nyttVedlegg, vedlegg);
    }

    @Test
    public void skalVaereUlikeVedleggVedUlikSkjemanummertillegg() {
        Vedlegg nyttVedlegg = new Vedlegg(SOKNAD_ID, FAKTUM_ID, SKJEMA_NUMMER, VedleggKreves);
        nyttVedlegg.setVedleggId(4L);
        nyttVedlegg.setSkjemanummerTillegg("annenkey");

        vedlegg.setSkjemanummerTillegg(TILLEGGSKEY);
        assertVedleggUlike(nyttVedlegg, vedlegg);
    }

    @Test
    public void skalVaereLikeVedleggVedBrukervalgtInnsendingsvalgOgVedleggKreves() {
        Vedlegg nyttVedlegg = new Vedlegg(SOKNAD_ID, FAKTUM_ID, SKJEMA_NUMMER, VedleggKreves);
        nyttVedlegg.setVedleggId(4L);

        vedlegg.setInnsendingsvalg(SendesIkke);
        assertVedleggLike(nyttVedlegg, vedlegg);

        vedlegg.setInnsendingsvalg(SendesSenere);
        assertVedleggLike(nyttVedlegg, vedlegg);

        vedlegg.setInnsendingsvalg(VedleggSendesAvAndre);
        assertVedleggLike(nyttVedlegg, vedlegg);

        vedlegg.setInnsendingsvalg(VedleggAlleredeSendt);
        assertVedleggLike(nyttVedlegg, vedlegg);

        vedlegg.setInnsendingsvalg(LastetOpp);
        assertVedleggLike(nyttVedlegg, vedlegg);
    }

    @Test
    public void skalVaereUlikeVedleggVedBrukervalgtInnsendingsvalgOgIkkeVedlegg() {
        Vedlegg nyttVedlegg = new Vedlegg(SOKNAD_ID, FAKTUM_ID, SKJEMA_NUMMER, IkkeVedlegg);
        nyttVedlegg.setVedleggId(4L);

        vedlegg.setInnsendingsvalg(SendesIkke);
        assertVedleggUlike(nyttVedlegg, vedlegg);

        vedlegg.setInnsendingsvalg(SendesSenere);
        assertVedleggUlike(nyttVedlegg, vedlegg);

        vedlegg.setInnsendingsvalg(VedleggSendesAvAndre);
        assertVedleggUlike(nyttVedlegg, vedlegg);

        vedlegg.setInnsendingsvalg(VedleggAlleredeSendt);
        assertVedleggUlike(nyttVedlegg, vedlegg);

        vedlegg.setInnsendingsvalg(LastetOpp);
        assertVedleggUlike(nyttVedlegg, vedlegg);
    }

    @Test
    public void skalVaereLikeVedleggIkkeVedlegg() {
        Vedlegg nyttVedlegg = new Vedlegg(SOKNAD_ID, FAKTUM_ID, SKJEMA_NUMMER, IkkeVedlegg);
        nyttVedlegg.setVedleggId(4L);

        vedlegg.setInnsendingsvalg(IkkeVedlegg);
        assertVedleggLike(nyttVedlegg, vedlegg);
    }

    @Test
    public void skalVaereLikeForListeMedGamleVedleggHvorEttErliktDetNye() {
        List<Vedlegg> gamleVedlegg = new ArrayList<>();

        Vedlegg gammeltVedleggTo = new Vedlegg(10L, 10L, SKJEMA_NUMMER, IkkeVedlegg);
        gammeltVedleggTo.setVedleggId(4L);

        gamleVedlegg.add(gammeltVedleggTo);
        gamleVedlegg.add(vedlegg);

        Vedlegg nyttVedlegg = new Vedlegg(SOKNAD_ID, FAKTUM_ID, SKJEMA_NUMMER, VedleggKreves);
        nyttVedlegg.setVedleggId(4L);

        assertThat(VedleggsgenereringUtil.likeVedlegg(gamleVedlegg, nyttVedlegg)).isEqualTo(true);
    }

    @Test
    public void skalVaereUlikeForListeMedGamleVedleggHvorIngenErLikNye() {
        List<Vedlegg> gamleVedlegg = new ArrayList<>();

        Vedlegg gammeltVedleggTo = new Vedlegg(10L, 10L, SKJEMA_NUMMER, IkkeVedlegg);
        gammeltVedleggTo.setVedleggId(4L);

        gamleVedlegg.add(gammeltVedleggTo);
        gamleVedlegg.add(vedlegg);

        Vedlegg nyttVedlegg = new Vedlegg(SOKNAD_ID, 111L, SKJEMA_NUMMER, IkkeVedlegg);
        nyttVedlegg.setVedleggId(4L);

        assertThat(VedleggsgenereringUtil.likeVedlegg(gamleVedlegg, nyttVedlegg)).isEqualTo(false);
    }

    @Test
    public void skalVaereLikeVedLikeVedleggslister() {
        List<Vedlegg> gamleVedlegg = new ArrayList<>();
        List<Vedlegg> nyeVedlegg = new ArrayList<>();

        Vedlegg gammeltVedleggTo = new Vedlegg(10L, 10L, SKJEMA_NUMMER, IkkeVedlegg);
        gammeltVedleggTo.setVedleggId(4L);

        gamleVedlegg.add(gammeltVedleggTo);
        gamleVedlegg.add(vedlegg);


        Vedlegg nyttVedleggLikSomVedlegg = new Vedlegg(SOKNAD_ID, FAKTUM_ID, SKJEMA_NUMMER, VedleggKreves);
        nyttVedleggLikSomVedlegg.setVedleggId(3L);

        Vedlegg nyttVedleggToLikSomGammeltTo = new Vedlegg(10L, 10L, SKJEMA_NUMMER, IkkeVedlegg);
        gammeltVedleggTo.setVedleggId(4L);

        nyeVedlegg.add(nyttVedleggLikSomVedlegg);
        nyeVedlegg.add(nyttVedleggToLikSomGammeltTo);

        assertVedleggListerLike(gamleVedlegg, nyeVedlegg);
    }


    @Test
    public void skalVaereUlikForUlikeVedleggslister() {
        List<Vedlegg> gamleVedlegg = new ArrayList<>();
        List<Vedlegg> nyeVedlegg = new ArrayList<>();

        Vedlegg gammeltVedleggTo = new Vedlegg(10L, 10L, SKJEMA_NUMMER, IkkeVedlegg);
        gammeltVedleggTo.setVedleggId(4L);

        gamleVedlegg.add(gammeltVedleggTo);
        gamleVedlegg.add(vedlegg);


        Vedlegg nyttVedlegg = new Vedlegg(SOKNAD_ID, FAKTUM_ID, SKJEMA_NUMMER, VedleggKreves);
        nyttVedlegg.setVedleggId(3L);

        Vedlegg nyttVedleggToUlikDeGamle = new Vedlegg(10L, 100L, SKJEMA_NUMMER, IkkeVedlegg);
        gammeltVedleggTo.setVedleggId(4L);

        nyeVedlegg.add(nyttVedleggToUlikDeGamle);
        nyeVedlegg.add(nyttVedlegg);

        assertVedleggListerUlike(gamleVedlegg, nyeVedlegg);
    }

    @Test
    public void skalVaereULikeVedleggForListeMedUlikStorrelse() {
        List<Vedlegg> gamleVedlegg = new ArrayList<>();
        List<Vedlegg> nyeVedlegg = new ArrayList<>();

        Vedlegg gammeltVedleggTo = new Vedlegg(10L, 10L, SKJEMA_NUMMER, IkkeVedlegg);
        gammeltVedleggTo.setVedleggId(4L);

        gamleVedlegg.add(gammeltVedleggTo);
        gamleVedlegg.add(vedlegg);


        Vedlegg nyttVedlegg = new Vedlegg(SOKNAD_ID, FAKTUM_ID, SKJEMA_NUMMER, VedleggKreves);
        nyttVedlegg.setVedleggId(3L);

        Vedlegg nyttVedleggToUlikDeGamle = new Vedlegg(10L, 10L, SKJEMA_NUMMER, IkkeVedlegg);
        gammeltVedleggTo.setVedleggId(4L);

        Vedlegg nyttVedleggTre = new Vedlegg(10L, 200L, SKJEMA_NUMMER, IkkeVedlegg);
        nyttVedleggTre.setVedleggId(4L);

        nyeVedlegg.add(nyttVedleggToUlikDeGamle);
        nyeVedlegg.add(nyttVedlegg);
        nyeVedlegg.add(nyttVedleggTre);

        assertVedleggListerUlike(gamleVedlegg, nyeVedlegg);
    }

    private void assertVedleggLike(Vedlegg nyttVedlegg, Vedlegg vedlegg) {
        assertThat(VedleggsgenereringUtil.likeVedlegg(vedlegg, nyttVedlegg)).isEqualTo(true);
    }

    private void assertVedleggUlike(Vedlegg nyttVedlegg, Vedlegg vedlegg) {
        assertThat(VedleggsgenereringUtil.likeVedlegg(vedlegg, nyttVedlegg)).isEqualTo(false);
    }

    private void assertVedleggListerLike(List<Vedlegg> gamleVedlegg, List<Vedlegg> nyeVedlegg) {
        assertThat(VedleggsgenereringUtil.likeVedlegg(gamleVedlegg, nyeVedlegg)).isEqualTo(true);
    }

    private void assertVedleggListerUlike(List<Vedlegg> gamleVedlegg, List<Vedlegg> nyeVedlegg) {
        assertThat(VedleggsgenereringUtil.likeVedlegg(gamleVedlegg, nyeVedlegg)).isEqualTo(false);
    }
}
