package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ArbeidsforholdBolk implements BolkService {

    private static final Logger LOG = LoggerFactory.getLogger(ArbeidsforholdBolk.class);

    public static final String BOLKNAVN = "Arbeidsforhold";

    private FaktaService faktaService;

    private ArbeidsforholdService arbeidsforholdService;
    
    
    

    public ArbeidsforholdBolk(FaktaService faktaService, ArbeidsforholdService arbeidsforholdService) {
		super();
		this.faktaService = faktaService;
		this.arbeidsforholdService = arbeidsforholdService;
	}

	protected void afterGenererArbeidsforhold(List<Faktum> arbeidsforholdFakta, final Long soknadId) {
        if (!arbeidsforholdFakta.isEmpty()) {
            Faktum yrkesaktiv = faktaService.hentFaktumMedKey(soknadId, "arbeidsforhold.yrkesaktiv");

            for (Faktum faktum : arbeidsforholdFakta) {
                faktum.setParrentFaktum(yrkesaktiv.getFaktumId());
            }

            if (maSetteYrkesaktiv(yrkesaktiv)) {
                arbeidsforholdFakta.add(yrkesaktiv.medValue("false"));
            }
        }
    }

    public List<Faktum> genererArbeidsforhold(String fodselsnummer, final Long soknadId) {

        ArbeidsforholdService.Sokeperiode sokeperiode = getSoekeperiode();

        List<Faktum> arbeidsforholdFakta = arbeidsforholdService.hentArbeidsforhold(fodselsnummer, sokeperiode).stream()
                .map(arbeidsforhold -> transformerTilFaktum(arbeidsforhold, soknadId))
                .collect(Collectors.toList());

        afterGenererArbeidsforhold(arbeidsforholdFakta, soknadId);

        return arbeidsforholdFakta;
    }

    ArbeidsforholdService.Sokeperiode getSoekeperiode() {
        return new ArbeidsforholdService.Sokeperiode(new DateTime().minusMonths(10), new DateTime());
    }


    private boolean maSetteYrkesaktiv(Faktum yrkesaktiv) {
        return yrkesaktiv.getValue() == null || "true".equals(yrkesaktiv.getValue());
    }

    private Faktum transformerTilFaktum(final Arbeidsforhold arbeidsforhold, final Long soknadId) {
        return new Faktum()
                .medSoknadId(soknadId)
                .medKey("arbeidsforhold")
                .medSystemProperty("orgnr", Objects.toString(arbeidsforhold.orgnr, ""))
                .medSystemProperty("arbeidsgivernavn", arbeidsforhold.arbeidsgivernavn)
                .medSystemProperty("ansatt", trueFalse(arbeidsforhold.tom == null))
                .medSystemProperty("fom", Objects.toString(arbeidsforhold.fom, ""))
                .medSystemProperty("tom", Objects.toString(arbeidsforhold.tom, ""))
                .medSystemProperty("land", "NO")
                .medSystemProperty("stillingstype", finnStillingsType(arbeidsforhold))
                .medSystemProperty("stillingsprosent", Objects.toString(arbeidsforhold.fastStillingsprosent, ""))
                .medSystemProperty("kilde", "EDAG")
                .medSystemProperty("edagref", "" + arbeidsforhold.edagId)
                .medUnikProperty("edagref");
    }


    private static String trueFalse(boolean test) {
        return test ? "true" : "false";
    }

    private static String finnStillingsType(Arbeidsforhold arbeidsforhold) {
        if (arbeidsforhold.harFastStilling && arbeidsforhold.variabelStillingsprosent) {
            return "fastOgVariabel";
        } else if (arbeidsforhold.harFastStilling) {
            return "fast";
        } else {
            return "variabel";
        }
    }

    @Override
    public String tilbyrBolk() {
        return BOLKNAVN;
    }

    @Override
    @Cacheable("arbeidsforholdCache")
    public List<Faktum> genererSystemFakta(String fodselsnummer, Long soknadId) {
        try {
            return genererArbeidsforhold(fodselsnummer, soknadId);
        } catch (Exception e) {
            LOG.warn("Kunne ikke hente arbeidsforhold: " + e, e);
            return Collections.emptyList();
        }
    }
}
