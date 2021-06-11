package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.dto.Land;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.joda.time.Months.monthsBetween;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArbeidsforholdBolkTest {

    @Mock
    private FaktaService faktaService;
    @Mock
    private ArbeidsforholdService arbeidsforholdService;
    @InjectMocks
    private ArbeidsforholdBolk arbeidsforholdBolk;

    private String tom = new DateTime().toString("yyyy-MM-dd");
    private String fom = new DateTime().minusYears(1).toString("yyyy-MM-dd");

    private Long yrkesAktivFaktumId = 1L;


    @Before
    public void setOpp() {
        when(faktaService.hentFaktumMedKey(anyLong(), eq("arbeidsforhold.yrkesaktiv")))
                .thenReturn(new Faktum().medKey("arbeidsforhold.yrkesaktiv").medValue("true").medFaktumId(yrkesAktivFaktumId));
    }

    @Test
    public void testGetSoekeperiode() {

        ArbeidsforholdService.Sokeperiode sokeperiode;

        sokeperiode = arbeidsforholdBolk.getSoekeperiode();

        DateTime fom = sokeperiode.getFom();
        DateTime tom = sokeperiode.getTom();

        assertEquals(-10, monthsBetween(tom, fom).getMonths());
    }

    @Test
    public void skalLagreSystemfakta() {
        no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold arbeidsforhold = lagArbeidsforhold();
        when(arbeidsforholdService.hentArbeidsforhold(any(String.class), any(ArbeidsforholdService.Sokeperiode.class))).thenReturn(Collections.singletonList(arbeidsforhold));

        List<Faktum> arbeidsforholdFakta = arbeidsforholdBolk.genererArbeidsforhold("123", 11L);
        assertEquals(2, arbeidsforholdFakta.size());
    }

    @Test
    public void skalSetteAlleFaktumFelter() {
        no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold arbeidsforhold = lagArbeidsforhold();
        when(arbeidsforholdService.hentArbeidsforhold(any(String.class), any(ArbeidsforholdService.Sokeperiode.class))).thenReturn(Collections.singletonList(arbeidsforhold));
        List<Faktum> faktums = arbeidsforholdBolk.genererArbeidsforhold("123", 11L);
        Faktum faktum = faktums.get(0);

        assertEquals("12345", faktum.finnEgenskap("orgnr").getValue());
        assertEquals("test", faktum.finnEgenskap("arbeidsgivernavn").getValue());
        assertEquals(fom + "", faktum.finnEgenskap("fom").getValue());
        assertEquals(tom + "", faktum.finnEgenskap("tom").getValue());
        assertEquals("NO", faktum.finnEgenskap("land").getValue());
        assertEquals("fast", faktum.finnEgenskap("stillingstype").getValue());
        assertEquals("50", faktum.finnEgenskap("stillingsprosent").getValue());
        assertEquals("EDAG", faktum.finnEgenskap("kilde").getValue());
        assertEquals("123", faktum.finnEgenskap("edagref").getValue());
    }

    @Test
    public void arbeidsforholdSkalHaRiktigParrentFaktum() {
        no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold arbeidsforhold = lagArbeidsforhold();
        when(arbeidsforholdService.hentArbeidsforhold(any(String.class), any(ArbeidsforholdService.Sokeperiode.class))).thenReturn(Collections.singletonList(arbeidsforhold));
        when(faktaService.hentFaktumMedKey(anyLong(), eq("arbeidsforhold.yrkesaktiv"))).thenReturn(new Faktum().medKey("arbeidsforhold.yrkesaktiv").medValue("false").medFaktumId(yrkesAktivFaktumId));

        List<Faktum> faktums = arbeidsforholdBolk.genererArbeidsforhold("123", 11L);
        Faktum faktum = faktums.get(0);

        assertEquals(yrkesAktivFaktumId, faktum.getParrentFaktum());
    }

    @Test
    public void skalSetteVariabel() {
        Arbeidsforhold arbeidsforhold = lagArbeidsforhold();
        when(arbeidsforholdService.hentArbeidsforhold(any(String.class), any(ArbeidsforholdService.Sokeperiode.class))).thenReturn(Collections.singletonList(arbeidsforhold));
        arbeidsforhold.harFastStilling = false;
        arbeidsforhold.fastStillingsprosent = 0L;
        arbeidsforhold.variabelStillingsprosent = true;
        List<Faktum> faktums = arbeidsforholdBolk.genererArbeidsforhold("123", 11L);
        Faktum faktum = faktums.get(0);

        assertEquals("variabel", faktum.finnEgenskap("stillingstype").getValue());
        assertEquals("0", faktum.finnEgenskap("stillingsprosent").getValue());
    }

    @Test
    public void skalSetteMixed() {
        no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold arbeidsforhold = lagArbeidsforhold();
        when(arbeidsforholdService.hentArbeidsforhold(any(String.class), any(ArbeidsforholdService.Sokeperiode.class))).thenReturn(Collections.singletonList(arbeidsforhold));

        arbeidsforhold.variabelStillingsprosent = true;
        List<Faktum> faktums = arbeidsforholdBolk.genererArbeidsforhold("123", 11L);
        Faktum faktum = faktums.get(0);

        assertEquals("fastOgVariabel", faktum.finnEgenskap("stillingstype").getValue());
        assertEquals("50", faktum.finnEgenskap("stillingsprosent").getValue());
    }

    @Test
    public void skalSettePagaende() {
        no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold arbeidsforhold = lagArbeidsforhold();
        when(arbeidsforholdService.hentArbeidsforhold(any(String.class), any(ArbeidsforholdService.Sokeperiode.class))).thenReturn(Collections.singletonList(arbeidsforhold));

        arbeidsforhold.tom = null;
        List<Faktum> faktums = arbeidsforholdBolk.genererArbeidsforhold("123", 11L);
        Faktum faktum = faktums.get(0);

        assertEquals("true", faktum.finnEgenskap("ansatt").getValue());
    }

    private no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold lagArbeidsforhold() {
        no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold arbeidsforhold = new no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold();
        arbeidsforhold.orgnr = "12345";
        arbeidsforhold.arbeidsgivernavn = "test";
        arbeidsforhold.harFastStilling = true;
        arbeidsforhold.variabelStillingsprosent = false;
        arbeidsforhold.land = new Land("norge", "NO");
        arbeidsforhold.fastStillingsprosent = 50L;
        arbeidsforhold.edagId = 123L;
        arbeidsforhold.fom = fom;
        arbeidsforhold.tom = tom;

        return arbeidsforhold;
    }
}
