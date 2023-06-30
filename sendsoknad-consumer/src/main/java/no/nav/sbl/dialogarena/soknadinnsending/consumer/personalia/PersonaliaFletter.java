package no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.PersonAlder;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.PersonaliaBuilder;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.AdresseTransform;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.IkkeFunnetException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.SikkerhetsBegrensningException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.EpostService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkonto;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontoNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontoUtland;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Statsborgerskap;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.xml.ws.WebServiceException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Denne klassen fletter sammen data fra 2 forskjellige endepunkter for å populere
 * domeneobjektet
 */
@Component
public class PersonaliaFletter {

    private static final Logger logger = getLogger(PersonaliaFletter.class);

    private PersonService personService;

    private BrukerprofilPortType brukerProfil;

    private Kodeverk kodeverk;

   
    private EpostService epostService;

    private static final String KJONN_MANN = "m";
    private static final String KJONN_KVINNE = "k";
    
    
    @Autowired
    public PersonaliaFletter(PersonService personService,@Qualifier("brukerProfilEndpoint") BrukerprofilPortType brukerProfil, Kodeverk kodeverk,
			EpostService epostService) {
		super();
		this.personService = personService;
		this.brukerProfil = brukerProfil;
		this.kodeverk = kodeverk;
		this.epostService = epostService;
	}

	public Personalia mapTilPersonalia(String fodselsnummer) {
        XMLHentKontaktinformasjonOgPreferanserResponse preferanserResponse;
        HentKjerneinformasjonResponse kjerneinformasjonResponse;
        try {
            preferanserResponse = brukerProfil.hentKontaktinformasjonOgPreferanser(lagXMLRequestPreferanser(fodselsnummer));
            kjerneinformasjonResponse = personService.hentKjerneinformasjon(fodselsnummer);
        } catch (IkkeFunnetException | HentKontaktinformasjonOgPreferanserPersonIkkeFunnet e) {
            logger.error("Ikke funnet person i TPS", e);
            throw new SendSoknadException("TPS:PersonIkkefunnet", e);
        } catch (HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning e) {
            logger.warn("Kunne ikke hente bruker fra TPS.", e);
            throw new SikkerhetsBegrensningException("TPS:Sikkerhetsbegrensing", e);
        } catch (WebServiceException e) {
            throw new SendSoknadException("TPS:webserviceException", e);
        }
        if (preferanserResponse == null) {
            return new Personalia();
        }

        XMLBruker xmlBruker = (XMLBruker) preferanserResponse.getPerson();
        Person xmlPerson = kjerneinformasjonResponse.getPerson();
        Diskresjonskoder diskresjonskode = kjerneinformasjonResponse.getPerson().getDiskresjonskode();
        String diskresjonskodeString = diskresjonskode == null ? null : diskresjonskode.getValue();

        EpostService.DigitalKontaktinfo dkifResponse = epostService.hentDigitalKontaktinfo(fodselsnummer);

        return PersonaliaBuilder.
                with()
                .fodselsnummer(finnFnr(xmlBruker))
                .fodselsdato(finnFodselsdato(xmlPerson))
                .alder(finnAlder(finnFnr(xmlBruker)))
                .diskresjonskode(diskresjonskodeString)
                .navn(finnSammensattNavn(xmlBruker))
                .withFornavn(finnFornavn(xmlBruker).trim())
                .withMellomnavn(finnMellomNavn(xmlBruker).trim())
                .withEtternavn(finnEtterNavn(xmlBruker))
                .epost(dkifResponse.epostadresse())
                .mobiltelefon(dkifResponse.mobiltelefonnummer())
                .statsborgerskap(finnStatsborgerskap(xmlPerson))
                .kjonn(finnKjonn(xmlBruker))
                .gjeldendeAdresse(finnGjeldendeAdresse(xmlBruker, kodeverk))
                .sekundarAdresse(finnSekundarAdresse(xmlBruker, kodeverk))
                .folkeregistrertAdresse(finnFolkeregistrertAdresse(xmlBruker, kodeverk))
                .kontonummer(finnKontonummer(xmlBruker))
                .erUtenlandskBankkonto(erUtenlandskKonto(xmlBruker))
                .utenlandskKontoBanknavn(finnUtenlandsKontoNavn(xmlBruker))
                .utenlandskKontoLand(finnUtenlandskKontoLand(xmlBruker, kodeverk))
                .build();
    }

    private static String finnUtenlandskKontoLand(XMLBruker xmlBruker, Kodeverk kodeverk) {
        XMLBankkonto bankkonto = xmlBruker.getBankkonto();

        if (bankkonto == null || bankkonto instanceof XMLBankkontoNorge) {
            return "";
        }
        String landkode = ((XMLBankkontoUtland) bankkonto).getBankkontoUtland().getLandkode().getValue();
        return kodeverk.getLand(landkode);
    }

    private static String finnUtenlandsKontoNavn(XMLBruker xmlBruker) {
        XMLBankkonto bankkonto = xmlBruker.getBankkonto();

        if (bankkonto == null || bankkonto instanceof XMLBankkontoNorge) {
            return "";
        }

        return ((XMLBankkontoUtland) bankkonto).getBankkontoUtland().getBanknavn();
    }

    private static Boolean erUtenlandskKonto(XMLBruker xmlBruker) {
        XMLBankkonto bankkonto = xmlBruker.getBankkonto();
        return bankkonto instanceof XMLBankkontoUtland;
    }

    private static String finnKontonummer(XMLBruker xmlBruker) {
        XMLBankkonto bankkonto = xmlBruker.getBankkonto();

        if (bankkonto instanceof XMLBankkontoUtland) {
            return ((XMLBankkontoUtland) bankkonto).getBankkontoUtland().getBankkontonummer();
        } else if (bankkonto instanceof XMLBankkontoNorge) {
            return ((XMLBankkontoNorge) bankkonto).getBankkonto().getBankkontonummer();
        } else {
            return "";
        }
    }

    private static Adresse finnGjeldendeAdresse(XMLBruker xmlBruker, Kodeverk kodeverk) {
        return new AdresseTransform().mapGjeldendeAdresse(xmlBruker, kodeverk);
    }
    
    private static Adresse finnFolkeregistrertAdresse(XMLBruker xmlBruker, Kodeverk kodeverk) {
        return new AdresseTransform().mapFolkeregistrertAdresse(xmlBruker, kodeverk);
    }

    private static Adresse finnSekundarAdresse(XMLBruker xmlBruker, Kodeverk kodeverk) {
        return new AdresseTransform().mapSekundarAdresse(xmlBruker, kodeverk);
    }

    private static String finnStatsborgerskap(Person xmlPerson) {
        if (xmlPerson.getStatsborgerskap() != null) {
            Statsborgerskap statsborgerskap = xmlPerson.getStatsborgerskap();
            return statsborgerskap.getLand().getValue();
        } else {
            return "NOR";
        }
    }

    private static LocalDate finnFodselsdato(Person person) {
        return new LocalDate(person.getFoedselsdato().getFoedselsdato().toGregorianCalendar());
    }


    private static String finnFnr(XMLBruker xmlBruker) {
        return xmlBruker.getIdent().getIdent();
    }

    private static String finnAlder(String fnr) {
        return String.valueOf(new PersonAlder(fnr).getAlder());
    }

    private static String finnKjonn(XMLBruker xmlBruker) {
        return Character.getNumericValue(finnFnr(xmlBruker).charAt(8)) % 2 == 0 ? KJONN_KVINNE : KJONN_MANN;
    }

    private static String finnSammensattNavn(XMLBruker xmlBruker) {
        if (fornavnExists(xmlBruker)) {
            return finnFornavn(xmlBruker) + finnMellomNavn(xmlBruker) + finnEtterNavn(xmlBruker);
        } else {
            return finnEtterNavn(xmlBruker);
        }
    }

    private static String finnFornavn(XMLBruker xmlBruker) {
        return fornavnExists(xmlBruker) ? xmlBruker.getPersonnavn().getFornavn() + " " : "";
    }

    private static boolean fornavnExists(XMLBruker xmlBruker) {
        return xmlBruker.getPersonnavn() != null && xmlBruker.getPersonnavn().getFornavn() != null;
    }

    private static String finnMellomNavn(XMLBruker xmlBruker) {
        return mellomnavnExists(xmlBruker) ? xmlBruker.getPersonnavn().getMellomnavn() + " " : "";
    }

    private static boolean mellomnavnExists(XMLBruker xmlBruker) {
        return xmlBruker.getPersonnavn() != null && xmlBruker.getPersonnavn().getMellomnavn() != null;
    }

    private static String finnEtterNavn(XMLBruker xmlBruker) {
        return etternavnExists(xmlBruker) ? xmlBruker.getPersonnavn().getEtternavn() : "";
    }

    private static boolean etternavnExists(XMLBruker xmlBruker) {
        return xmlBruker.getPersonnavn() != null && xmlBruker.getPersonnavn().getEtternavn() != null;
    }

    private XMLHentKontaktinformasjonOgPreferanserRequest lagXMLRequestPreferanser(String ident) {
        return new XMLHentKontaktinformasjonOgPreferanserRequest().withIdent(ident);
    }
}
