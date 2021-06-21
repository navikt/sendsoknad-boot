package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;

@Component
public class CmsTekst {
    @Autowired
    private TekstHenter tekstHenter;

    private static final Logger LOG = LoggerFactory.getLogger(CmsTekst.class);


    public String getCmsTekst(String key, Object[] parameters, String soknadTypePrefix, String bundleName, Locale locale) {
        Properties bundle = tekstHenter.getBundleFor(bundleName, locale);

        String tekst = bundle.getProperty(soknadTypePrefix + "." + key);

        if (tekst == null) {
            tekst = bundle.getProperty(key);
        }

        if (tekst == null) {
            LOG.debug(String.format("Fant ikke tekst til oppsummering for nokkel %s i bundelen %s", key, bundleName));
            return tekst;
        } else {
            return MessageFormat.format(tekst, parameters);
        }
    }
}