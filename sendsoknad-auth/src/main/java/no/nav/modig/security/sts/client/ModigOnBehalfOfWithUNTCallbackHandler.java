package no.nav.modig.security.sts.client;

import java.io.IOException;
import java.io.StringReader;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.core.domain.IdentType;

import org.apache.cxf.ws.security.trust.delegation.DelegationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ModigOnBehalfOfWithUNTCallbackHandler implements CallbackHandler {

    private static final Logger logger = LoggerFactory.getLogger(ModigOnBehalfOfWithUNTCallbackHandler.class);

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof DelegationCallback) {
                DelegationCallback delegationCallback = (DelegationCallback) callback;
                delegationCallback.setToken(getElement());
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    private Element getElement() throws IOException {
        SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();
        IdentType identType = subjectHandler.getIdentType();

        if (IdentType.Systemressurs.equals(identType)) {
            return null;
        } else if (IdentType.InternBruker.equals(identType)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            DocumentBuilder builder;
            Document document = null;

            try {
                builder = factory.newDocumentBuilder();
                document = builder.parse(new InputSource(new StringReader(getOnBehalfOfString(subjectHandler))));
            } catch (ParserConfigurationException e) {
                logger.error("Exception while getting builder, aborting", e);
                throw new RuntimeException(e);
            } catch (SAXException e) {
                logger.error("Exception while getting OnBehalfOf element, aborting", e);
                throw new RuntimeException(e);
            }

            return document.getDocumentElement();
        }
        throw new IllegalStateException("wst:OnBehalfOf is only supported for IdentType.Systemressurs and IdentType.InternBruker");
    }


    private String getOnBehalfOfString(SubjectHandler subjectHandler) {
        return "<wsse:UsernameToken xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" +
        		"<wsse:Username>" + subjectHandler.getUid() + "</wsse:Username>" + 
                "</wsse:UsernameToken>";
    }

}
