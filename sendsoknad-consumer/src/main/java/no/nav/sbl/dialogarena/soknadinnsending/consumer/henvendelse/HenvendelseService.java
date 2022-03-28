package no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse;

import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.IKKE_VALGT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType.SEND_SOKNAD_ETTERSENDING;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Optional;

import javax.xml.ws.soap.SOAPFaultException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLSoknadMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.tokensupport.TokenUtils;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.henvendelse.HenvendelsePortType;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentHenvendelseRequest;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentHenvendelseResponse;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSSoknadsdata;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;

@Component
public class HenvendelseService {

    private static final Logger logger = getLogger(HenvendelseService.class);

    private SendSoknadPortType sendSoknadEndpoint;
   
    private SendSoknadPortType sendSoknadSelftestEndpoint;
    
    private HenvendelsePortType henvendelseInformasjonEndpoint;
    
    

    @Autowired
    public HenvendelseService(@Qualifier("sendSoknadEndpoint")
			SendSoknadPortType sendSoknadEndpoint,
		@Qualifier("sendSoknadSelftestEndpoint")SendSoknadPortType sendSoknadSelftestEndpoint,
			HenvendelsePortType henvendelseInformasjonEndpoint) {
		super();
		this.sendSoknadEndpoint = sendSoknadEndpoint;
		this.sendSoknadSelftestEndpoint = sendSoknadSelftestEndpoint;
		this.henvendelseInformasjonEndpoint = henvendelseInformasjonEndpoint;
	}

	public String startSoknad(String fnr, String skjemanummer, String tilleggsinfo, String uuid, SoknadType soknadType) {
        logger.info("Søknad startet med skjemanummer " + skjemanummer  + " av typen" + soknadType);

        XMLMetadataListe xmlMetadataListe = new XMLMetadataListe().withMetadata(createXMLSkjema(skjemanummer, tilleggsinfo, uuid));
        WSStartSoknadRequest startSoknadRequest = lagOpprettSoknadRequest(fnr, soknadType, xmlMetadataListe);

        return opprettSoknadIHenvendelse(startSoknadRequest);
    }

    public String startEttersending(WSHentSoknadResponse soknadResponse, String aktorId) {
        logger.info("Ettersending startes knyttet til søknad med behandlingsID: " + soknadResponse.getBehandlingsId());

        String behandlingskjedeId = Optional.ofNullable(soknadResponse.getBehandlingskjedeId()).orElse(soknadResponse.getBehandlingsId());

        return opprettSoknadIHenvendelse(
                lagOpprettSoknadRequest(aktorId, SEND_SOKNAD_ETTERSENDING, (XMLMetadataListe) soknadResponse.getAny())
                        .withBehandlingskjedeId(behandlingskjedeId));
    }

    public List<WSBehandlingskjedeElement> hentBehandlingskjede(String behandlingskjedeId) {
        try {
            List<WSBehandlingskjedeElement> wsBehandlingskjedeElementer = sendSoknadEndpoint.hentBehandlingskjede(behandlingskjedeId);
            if (wsBehandlingskjedeElementer.isEmpty()) {
                throw new SendSoknadException("Fant ingen behandlinger i en behandlingskjede med behandlingsID " + behandlingskjedeId);
            }
            return wsBehandlingskjedeElementer;
        } catch (SOAPFaultException e) {
            throw new SendSoknadException("Kunne ikke hente behandlingskjede", e, "exception.system.baksystem");
        }
    }

    public void avsluttSoknad(String behandlingsId, XMLHovedskjema hovedskjema, XMLVedlegg[] vedlegg, XMLSoknadMetadata ekstraData) {
        try {
            XMLMetadataListe metadataliste = new XMLMetadataListe()
                    .withMetadata(hovedskjema)
                    .withMetadata(vedlegg);

            if (ekstraData.getVerdi().size() > 0) {
                metadataliste.withMetadata(ekstraData);
            }

            WSSoknadsdata parameters = new WSSoknadsdata().withBehandlingsId(behandlingsId).withAny(metadataliste);

            logger.info("Søknad avsluttet " + behandlingsId + " " + hovedskjema.getSkjemanummer() + " (" + hovedskjema.getJournalforendeEnhet() + ") " + vedlegg.length + " vedlegg");
            sendSoknadEndpoint.sendSoknad(parameters);
        } catch (SOAPFaultException e) {
            throw new SendSoknadException("Kunne ikke sende inn søknad", e, "exception.system.baksystem");
        }
    }

    public WSHentSoknadResponse hentSoknad(String behandlingsId) {
        return sendSoknadEndpoint.hentSoknad(new WSBehandlingsId().withBehandlingsId(behandlingsId));
    }

    public void avbrytSoknad(String behandlingsId) {
        logger.info("Søknad avbrutt for " + behandlingsId);
        try {
            SendSoknadPortType sendSoknadPortType = sendSoknadEndpoint;
            if (TokenUtils.getSubject() == null) {
                sendSoknadPortType = sendSoknadSelftestEndpoint;
                logger.info("Bruker systembruker for avbrytkall " );
            }
            sendSoknadPortType.avbrytSoknad(behandlingsId);
        } catch (SOAPFaultException e) {
            throw new SendSoknadException("Kunne ikke avbryte søknad", e, "exception.system.baksystem");
        }
    }

    private String opprettSoknadIHenvendelse(WSStartSoknadRequest startSoknadRequest) {
        try {
        	
            return sendSoknadEndpoint.startSoknad(startSoknadRequest).getBehandlingsId();
        } catch (SOAPFaultException e) {
            throw new SendSoknadException("Kunne ikke opprette ny søknad", e, "exception.system.baksystem");
        }
    }

    public XMLHenvendelse hentInformasjonOmAvsluttetSoknad(String behandlingsId) {
        WSHentHenvendelseResponse wsHentHenvendelseResponse = henvendelseInformasjonEndpoint.hentHenvendelse(
                new WSHentHenvendelseRequest()
                        .withBehandlingsId(behandlingsId));
        return (XMLHenvendelse) wsHentHenvendelseResponse.getAny();
    }

    private WSStartSoknadRequest lagOpprettSoknadRequest(String fnr, SoknadType soknadType, XMLMetadataListe xmlMetadataListe) {
        return new WSStartSoknadRequest()
                .withFodselsnummer(fnr)
                .withType(soknadType.name())
                .withBehandlingskjedeId("")
                .withAny(xmlMetadataListe);
    }

    private XMLHovedskjema createXMLSkjema(String skjemanummer, String tilleggsinfo, String uuid) {
        return new XMLHovedskjema()
                .withSkjemanummer(skjemanummer)
                .withTilleggsinfo(tilleggsinfo)
                .withUuid(uuid)
                .withInnsendingsvalg(IKKE_VALGT.toString());
    }
}
