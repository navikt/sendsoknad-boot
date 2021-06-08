package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.*;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ArbeidsforholdTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArbeidsforholdTransformer.class);

   
    private OrganisasjonV4 organisasjonWebService;

    
    @Autowired
    public ArbeidsforholdTransformer(@Qualifier("organisasjonEndpoint") OrganisasjonV4 organisasjonWebService) {
		super();
		this.organisasjonWebService = organisasjonWebService;
	}

	public Arbeidsforhold transform(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        Arbeidsforhold result = new Arbeidsforhold();
        result.edagId = arbeidsforhold.getArbeidsforholdIDnav();
        result.orgnr = null;
        result.arbeidsgivernavn = "";

        if(arbeidsforhold.getArbeidsgiver() instanceof Organisasjon) {
            result.orgnr = ((Organisasjon) arbeidsforhold.getArbeidsgiver()).getOrgnummer();
            result.arbeidsgivernavn = hentOrgNavn(result.orgnr);
        }
        else if (arbeidsforhold.getArbeidsgiver() instanceof HistoriskArbeidsgiverMedArbeidsgivernummer) {
            result.arbeidsgivernavn = ((HistoriskArbeidsgiverMedArbeidsgivernummer) arbeidsforhold.getArbeidsgiver()).getNavn();
        }
        else if(arbeidsforhold.getArbeidsgiver() instanceof Person) {
            result.arbeidsgivernavn = "Privatperson";
        }

        Gyldighetsperiode periode = arbeidsforhold.getAnsettelsesPeriode().getPeriode();
        result.fom = toStringDate(periode.getFom());
        result.tom = toStringDate(periode.getTom());

        if (arbeidsforhold.getArbeidsavtale() != null) {
            for (Arbeidsavtale arbeidsavtale : arbeidsforhold.getArbeidsavtale()) {
                result.harFastStilling = true;
                result.fastStillingsprosent += nullSafe(arbeidsavtale.getStillingsprosent());
            }
        }
        return result;
    }

    private String toStringDate(XMLGregorianCalendar fom) {
        return fom != null ? new DateTime(fom.toGregorianCalendar()).toString("yyyy-MM-dd") : null;
    }

    private Long nullSafe(BigDecimal number) {
        return number != null ? number.longValue() : 0;
    }


    private String hentOrgNavn(String orgnr) {
        if (orgnr != null) {
            HentOrganisasjonRequest hentOrganisasjonRequest = lagOrgRequest(orgnr);
            try {
                //Kan bare være ustrukturert navn.
                List<String> navnelinje = ((UstrukturertNavn) organisasjonWebService.hentOrganisasjon(hentOrganisasjonRequest).getOrganisasjon().getNavn()).getNavnelinje();
                return String.join(", ", navnelinje);
            } catch (Exception ex) {
                LOGGER.warn("Kunne ikke hente orgnr: " + orgnr, ex);
                return "";
            }
        } else {
            return "";
        }
    }

    private HentOrganisasjonRequest lagOrgRequest(String orgnr) {
        HentOrganisasjonRequest hentOrganisasjonRequest = new HentOrganisasjonRequest();
        hentOrganisasjonRequest.setOrgnummer(orgnr);
        hentOrganisasjonRequest.setInkluderHierarki(false);
        hentOrganisasjonRequest.setInkluderHistorikk(false);
        return hentOrganisasjonRequest;
    }
}
