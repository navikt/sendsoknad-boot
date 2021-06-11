package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;

@Service
public class BarnBolk implements BolkService {

    public static final String BOLKNAVN = "Barn";

    
    private PersonService personService;
    
    
    @Autowired
    public BarnBolk(PersonService personService) {
		super();
		this.personService = personService;
	}

	@Override
    public String tilbyrBolk() {
        return BOLKNAVN;
    }

    @Override
    public List<Faktum> genererSystemFakta(String fodselsnummer, final Long soknadId) {
        return personService.hentBarn(fodselsnummer).stream()
                .map(barn ->
                    new Faktum().medSoknadId(soknadId).medKey("barn").medType(SYSTEMREGISTRERT)
                        .medSystemProperty("fornavn", barn.getFornavn())
                        .medSystemProperty("mellomnavn", barn.getMellomnavn())
                        .medSystemProperty("etternavn", barn.getEtternavn())
                        .medSystemProperty("sammensattnavn", barn.getSammensattnavn())
                        .medSystemProperty("fnr", barn.getFnr())
                        .medSystemProperty("kjonn", barn.getKjonn())
                        .medSystemProperty("alder", barn.getAlder().toString())
                        .medSystemProperty("land", barn.getLand())
                        .medUnikProperty("fnr")
                ).collect(Collectors.toList());
    }
}
