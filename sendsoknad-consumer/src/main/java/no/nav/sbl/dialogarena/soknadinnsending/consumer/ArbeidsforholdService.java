package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.NorskIdent;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Periode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Regelverker;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeFactory;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils.lagDatatypeFactory;

@Service
public class ArbeidsforholdService {

   
    private ArbeidsforholdV3 arbeidsforholdWebWervice;

    private ArbeidsforholdTransformer arbeidsforholdTransformer;

    private static final Regelverker AA_ORDNINGEN = new Regelverker();
    static {
        AA_ORDNINGEN.setValue("A_ORDNINGEN");
    }

    private DatatypeFactory datatypeFactory;
    
    

    @Autowired
    public ArbeidsforholdService(@Qualifier("arbeidEndpoint") ArbeidsforholdV3 arbeidsforholdWebWervice,
			ArbeidsforholdTransformer arbeidsforholdTransformer) {
		super();
		this.arbeidsforholdWebWervice = arbeidsforholdWebWervice;
		this.arbeidsforholdTransformer = arbeidsforholdTransformer;
		this.datatypeFactory = lagDatatypeFactory();;
	}

	public List<Arbeidsforhold> hentArbeidsforhold(String fodselsnummer, Sokeperiode soekeperiode) {
        try {
            FinnArbeidsforholdPrArbeidstakerRequest finnArbeidsforholdPrArbeidstakerRequest =
                    lagArbeidsforholdRequest(fodselsnummer, lagPeriode(soekeperiode.fom, soekeperiode.tom));

            List<no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold> arbeidsforhold =
                    arbeidsforholdWebWervice.finnArbeidsforholdPrArbeidstaker(finnArbeidsforholdPrArbeidstakerRequest).getArbeidsforhold();

            return arbeidsforhold.stream()
                    .map(af -> arbeidsforholdTransformer.transform(af))
                    .collect(Collectors.toList());

        } catch (FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning | FinnArbeidsforholdPrArbeidstakerUgyldigInput e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private FinnArbeidsforholdPrArbeidstakerRequest lagArbeidsforholdRequest(String fodselsnummer, Periode periode ) {
        FinnArbeidsforholdPrArbeidstakerRequest request = new FinnArbeidsforholdPrArbeidstakerRequest();
        request.setArbeidsforholdIPeriode(periode);
        request.setRapportertSomRegelverk(AA_ORDNINGEN);
        request.setIdent(lagIdent(fodselsnummer));
        return request;
    }

    private NorskIdent lagIdent(String fodselsnummer) {
        NorskIdent ident = new NorskIdent();
        ident.setIdent(fodselsnummer);
        return ident;
    }

    private Periode lagPeriode(DateTime fom, DateTime tom) {
        Periode periode = new Periode();
        periode.setFom(datatypeFactory.newXMLGregorianCalendar(fom.toGregorianCalendar()));
        periode.setTom(datatypeFactory.newXMLGregorianCalendar(tom.toGregorianCalendar()));
        return periode;
    }

    public static final class Sokeperiode {

        private final DateTime fom;
        private final DateTime tom;

        public Sokeperiode(DateTime fom, DateTime tom) {
            this.fom = fom;
            this.tom = tom;
        }

        public DateTime getFom() {
            return fom;
        }

        public DateTime getTom() {
            return tom;
        }
    }
}
