package no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.dto.Land;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.LandService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LandOgPostInfoFetcherService {

    
    private LandService landService;
    
    private Kodeverk kodeverk;
    
    
    @Autowired
    public LandOgPostInfoFetcherService(LandService landService, Kodeverk kodeverk) {
		super();
		this.landService = landService;
		this.kodeverk = kodeverk;
	}

	public List<Land> hentLand(String filter) {
        return landService.hentLand(filter);
    }

    public String getLandnavn(String landkode) {
        return landService.getLandnavn(landkode);
    }

    public String getPoststed(String postnummer) {
        return kodeverk.getPoststed(postnummer);
    }
}
