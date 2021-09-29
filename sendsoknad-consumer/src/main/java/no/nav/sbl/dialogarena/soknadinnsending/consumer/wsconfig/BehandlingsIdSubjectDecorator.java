package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.MDC;

import no.nav.modig.common.MDCOperations;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;

public class BehandlingsIdSubjectDecorator implements InvocationHandler {
	
	private static final Logger logger = getLogger(BehandlingsIdSubjectDecorator.class);
	
	private SendSoknadPortType sendSoknadPort;
	
	

	public BehandlingsIdSubjectDecorator(SendSoknadPortType sendSoknadPort) {
		super();
		this.sendSoknadPort = sendSoknadPort;
	}



	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		logger.info("Entering behandlingsID interceptor");
		
		if (method.getName().equals("startSoknad")) {
		
			String behandlingsId = ((WSBehandlingsId)method.invoke(sendSoknadPort, args)).getBehandlingsId();
			
			SubjectHandler.getSubjectHandler().setBehandlingsId( behandlingsId );
			
			MDCOperations.putToMDC(MDCOperations.MDC_BEHANDLINGS_ID, behandlingsId);
		
			return behandlingsId;
		}
		else {
			return method.invoke(sendSoknadPort, args);
		}

	}

	
	
}
