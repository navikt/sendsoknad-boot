package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.dto.Land;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.LandListe.EOS;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.LandListe.EOS_LAND;

@Component
public class LandService {

    
    private Kodeverk kodeverk;

    public List<Land> hentLand(String filter) {
        if (EOS.equals(filter)) {
            return hentEosLand();
        }
        return hentAlleLand();
    }
    
    
    @Autowired
    public LandService(Kodeverk kodeverk) {
		super();
		this.kodeverk = kodeverk;
	}



	private List<Land> hentAlleLand() {
        List<Land> landliste = new ArrayList<>();
        List<String> landKoder = kodeverk.hentAlleKodenavnFraKodeverk(Kodeverk.EksponertKodeverk.LANDKODE);

        for (String landkode : landKoder) {
            Land land = new Land();
            land.setText(kodeverk.getLand(landkode));
            land.setValue(landkode);
            if (land.getText() != null) {
                landliste.add(land);
            }
        }
        if (!landKoder.contains("NOR")) {
            Land norge = new Land("Norge", "NOR");

            List<Land> alleAndreLand = landliste;
            landliste = new ArrayList<>();
            landliste.add(norge);
            landliste.addAll(alleAndreLand);
        }

        return landliste;
    }

    private List<Land> hentEosLand() {
        List<Land> landliste = new ArrayList<>();

        for (String landkode : EOS_LAND) {
            Land land = new Land();
            String landnavn = kodeverk.getLand(landkode);
            landnavn = landnavn != null ? landnavn : landkode;
            land.setText(landnavn);
            land.setValue(landkode);
            landliste.add(land);
        }
        return landliste;
    }

    public String getLandnavn(String landkode) {
        return kodeverk.getLand(landkode);
    }
}
