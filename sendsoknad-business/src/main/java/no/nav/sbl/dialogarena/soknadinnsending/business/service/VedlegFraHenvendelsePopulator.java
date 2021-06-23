package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPUtlandetInformasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.meldinger.WSInnhold;

@Component
public class VedlegFraHenvendelsePopulator {
	
	 private VedleggRepository vedleggRepository;

	 
	 @Autowired
	 public VedlegFraHenvendelsePopulator(@Qualifier("vedleggRepository") VedleggRepository vedleggRepository) {
		super();
		this.vedleggRepository = vedleggRepository;
	 }

	 public List<Vedlegg> hentVedleggOgKvittering(WebSoknad soknad) {
	        ArrayList<Vedlegg> vedleggForventninger = new ArrayList<>(soknad.hentValidertVedlegg());
	        final String AAP_UTLAND_SKJEMANUMMER = new AAPUtlandetInformasjon().getSkjemanummer().get(0);
	        if (!AAP_UTLAND_SKJEMANUMMER.equals(soknad.getskjemaNummer())) {
	            Vedlegg kvittering = vedleggRepository.hentVedleggForskjemaNummer(soknad.getSoknadId(), null, SKJEMANUMMER_KVITTERING);

	            if (kvittering != null) {
	                vedleggForventninger.add(kvittering);
	            }
	        }
	        return vedleggForventninger;
	    }



	public void populerVedleggMedDataFraHenvendelse(WebSoknad soknad, List<WSInnhold> innhold) {
	        for (WSInnhold wsInnhold : innhold) {
	            byte[] vedleggData;

	            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
	                wsInnhold.getInnhold().writeTo(outputStream);
	                vedleggData = outputStream.toByteArray();
	            } catch (IOException e) {
	                throw new SendSoknadException("Kunne ikke hente opp soknaddata", e);
	            }

	            Vedlegg vedlegg = soknad.hentVedleggMedUID(wsInnhold.getUuid());
	            if (vedlegg != null) {
	                vedlegg.setData(vedleggData);
	                vedleggRepository.lagreVedleggMedData(soknad.getSoknadId(), vedlegg.getVedleggId(), vedlegg);
	            }
	        }
	    }

}
