package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;

import java.util.ArrayList;
import java.util.List;

public class FamilierelasjonTransform {

    public static List<Barn> mapFamilierelasjon(HentKjerneinformasjonResponse response) {
        if (response == null) {
            return new ArrayList<>();
        }

        return finnBarn(response.getPerson(), 0L);
    }

    private static List<Barn> finnBarn(Person xmlperson, Long soknadId) {
        List<Barn> result = new ArrayList<>();

        List<Familierelasjon> familierelasjoner = xmlperson.getHarFraRolleI();
        if (familierelasjoner.isEmpty()) {
            return result;
        }
        for (Familierelasjon familierelasjon : familierelasjoner) {
            Familierelasjoner familierelasjonType = familierelasjon.getTilRolle();
            if (familierelasjonType.getValue().equals("BARN")) {
                no.nav.tjeneste.virksomhet.person.v1.informasjon.Person tilPerson = familierelasjon.getTilPerson();
                Barn barn = mapXmlPersonToPerson(tilPerson, soknadId);
                if (barn.getAlder() < 18 && !isDoed(barn)) {
                    result.add(barn);
                }
            }
        }

        return result;
    }

    private static boolean isDoed(Barn barn) {
        String personstatus = barn.getPersonstatus();
        return barn.getDoedsdato() != null || "DØD".equals(personstatus);
    }

    private static Barn mapXmlPersonToPerson(Person xmlperson, Long soknadId) {
        return new Barn(
                soknadId,
                finnDoedsDato(xmlperson),
                finnPersonStatus(xmlperson),
                finnFnr(xmlperson),
                finnFornavn(xmlperson),
                finnMellomNavn(xmlperson),
                finnEtterNavn(xmlperson)).withLand(finnStatsborgerskap(xmlperson));
    }

    private static String finnPersonStatus(Person xmlperson) {
        Personstatus personstatus = xmlperson.getPersonstatus();
        if (personstatus != null && personstatus.getPersonstatus() != null) {
            return personstatus.getPersonstatus().getValue();
        }
        return "";
    }

    private static String finnDoedsDato(Person xmlperson) {
        Doedsdato doedsdato = xmlperson.getDoedsdato();
        if (doedsdato != null && doedsdato.getDoedsdato() != null) {
            return doedsdato.getDoedsdato().toString();
        }
        return null;
    }

    private static String finnStatsborgerskap(no.nav.tjeneste.virksomhet.person.v1.informasjon.Person soapPerson) {
        if (soapPerson.getStatsborgerskap() != null) {
            Statsborgerskap statsborgerskap = soapPerson.getStatsborgerskap();
            return statsborgerskap.getLand().getValue();
        } else {
            return "NOR";
        }
    }

    private static String finnFornavn(Person soapPerson) {
        return fornavnExists(soapPerson) ? soapPerson.getPersonnavn().getFornavn() : "";
    }

    private static boolean fornavnExists(Person soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getFornavn() != null;
    }

    private static String finnMellomNavn(Person soapPerson) {
        return mellomnavnExists(soapPerson) ? soapPerson.getPersonnavn().getMellomnavn() : "";
    }

    private static boolean mellomnavnExists(Person soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getMellomnavn() != null;
    }

    private static String finnEtterNavn(Person soapPerson) {
        return etternavnExists(soapPerson) ? soapPerson.getPersonnavn().getEtternavn() : "";
    }

    private static boolean etternavnExists(Person soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getEtternavn() != null;
    }

    private static String finnFnr(Person xmlperson) {
        return xmlperson.getIdent().getIdent();
    }
}
