package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.StatsborgerskapType;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia.*;

@Service
public class PersonaliaBolk implements BolkService {

    public static final String BOLKNAVN = "Personalia";

    private PersonaliaFletter personaliaFletter;
    
    
    @Autowired
    public PersonaliaBolk(PersonaliaFletter personaliaFletter) {
		super();
		this.personaliaFletter = personaliaFletter;
	}

	public Personalia hentPersonalia(String fodselsnummer) {
        return personaliaFletter.mapTilPersonalia(fodselsnummer);
    }

    @Override
    public String tilbyrBolk() {
        return BOLKNAVN;
    }

    @Override
    public List<Faktum> genererSystemFakta(String fodselsnummer, Long soknadId) {
        return genererPersonaliaFaktum(soknadId, personaliaFletter.mapTilPersonalia(fodselsnummer));
    }

    private List<Faktum> genererPersonaliaFaktum(Long soknadId, Personalia personalia) {
        String statsborgerskap = personalia.getStatsborgerskap();
        return singletonList(new Faktum().medSoknadId(soknadId).medKey("personalia")
                .medSystemProperty(FNR_KEY, personalia.getFnr())
                .medSystemProperty(KONTONUMMER_KEY, personalia.getKontonummer())
                .medSystemProperty(ER_UTENLANDSK_BANKKONTO, personalia.getErUtenlandskBankkonto().toString())
                .medSystemProperty(UTENLANDSK_KONTO_BANKNAVN, personalia.getUtenlandskKontoBanknavn())
                .medSystemProperty(UTENLANDSK_KONTO_LAND, personalia.getUtenlandskKontoLand())
                .medSystemProperty(ALDER_KEY, personalia.getAlder())
                .medSystemProperty(NAVN_KEY, personalia.getNavn())
                .medSystemProperty(FORNAVN_KEY, personalia.getFornavn())
                .medSystemProperty(MELLOMNAVN_KEY, personalia.getMellomnavn())
                .medSystemProperty(ETTERNAVN_KEY, personalia.getEtternavn())
                .medSystemProperty(EPOST_KEY, personalia.getEpost())
                .medSystemProperty(STATSBORGERSKAP_KEY, statsborgerskap)
                .medSystemProperty(STATSBORGERSKAPTYPE_KEY, StatsborgerskapType.get(statsborgerskap))
                .medSystemProperty(KJONN_KEY, personalia.getKjonn())
                .medSystemProperty(GJELDENDEADRESSE_KEY, personalia.getGjeldendeAdresse().getAdresse())
                .medSystemProperty(DISKRESJONSKODE, personalia.getDiskresjonskode())
                .medSystemProperty(GJELDENDEADRESSE_TYPE_KEY, personalia.getGjeldendeAdresse().getAdressetype())
                .medSystemProperty(GJELDENDEADRESSE_GYLDIGFRA_KEY, personalia.getGjeldendeAdresse().getGyldigFra())
                .medSystemProperty(GJELDENDEADRESSE_GYLDIGTIL_KEY, personalia.getGjeldendeAdresse().getGyldigTil())
                .medSystemProperty(GJELDENDEADRESSE_LANDKODE, personalia.getGjeldendeAdresse().getLandkode())
                .medSystemProperty(SEKUNDARADRESSE_KEY, personalia.getSekundarAdresse().getAdresse())
                .medSystemProperty(SEKUNDARADRESSE_TYPE_KEY, personalia.getSekundarAdresse().getAdressetype())
                .medSystemProperty(SEKUNDARADRESSE_GYLDIGFRA_KEY, personalia.getSekundarAdresse().getGyldigFra())
                .medSystemProperty(SEKUNDARADRESSE_GYLDIGTIL_KEY, personalia.getSekundarAdresse().getGyldigTil()));
    }
}
