package no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.AktivitetService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.MaalgrupperService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AktivitetOgMaalgrupperFetcherService {

    
    private AktivitetService aktivitetService;
    
    private MaalgrupperService maalgrupperService;
    
    
    @Autowired
    public AktivitetOgMaalgrupperFetcherService(AktivitetService aktivitetService,
			MaalgrupperService maalgrupperService) {
		super();
		this.aktivitetService = aktivitetService;
		this.maalgrupperService = maalgrupperService;
	}

	public List<Faktum> hentAktiviteter(String fodselnummer) {
        return aktivitetService.hentAktiviteter(fodselnummer);
    }

    public List<Faktum> hentVedtak(String fodselsnummer) {
        return aktivitetService.hentVedtak(fodselsnummer);
    }

    public List<Faktum> hentMaalgrupper(String fodselsnummer) {
        return maalgrupperService.hentMaalgrupper(fodselsnummer);
    }
}
