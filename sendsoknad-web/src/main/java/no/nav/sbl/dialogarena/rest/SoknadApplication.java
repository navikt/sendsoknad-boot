package no.nav.sbl.dialogarena.rest;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import no.nav.sbl.dialogarena.rest.actions.SoknadActions;
import no.nav.sbl.dialogarena.rest.feil.ApplicationExceptionMapper;
import no.nav.sbl.dialogarena.rest.feil.ThrowableMapper;
import no.nav.sbl.dialogarena.rest.providers.JsonToTextPlainBodyWriter;
import no.nav.sbl.dialogarena.rest.providers.SoknadObjectMapperProvider;
import no.nav.sbl.dialogarena.rest.ressurser.FaktaRessurs;
import no.nav.sbl.dialogarena.rest.ressurser.SoknadRessurs;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggRessurs;
import no.nav.sbl.dialogarena.rest.ressurser.informasjon.InformasjonRessurs;
import no.nav.sbl.dialogarena.rest.ressurser.informasjon.TestController;
import no.nav.sbl.dialogarena.rest.ressurser.informasjon.TjenesterRessurs;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Jersey 2 config
 */
@Configuration
//@ApplicationPath("/")
public class SoknadApplication extends ResourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(SoknadApplication.class);

    public SoknadApplication() {
        // JacksonJaxbJsonProvider registreres manuelt for å unngå å dra inn Jacksons egne ExceptionMappers, som
        // returnerer litt for mye informasjon i sine feilmeldinger. Desse ExceptionMappers har @Provider-annotationer
        // og blir automatisk trukket inn hvis du tar tar inn hele Jackson-pakken for JSON.
      //  packages("no.nav.sbl.dialogarena.rest");
        register(JacksonJaxbJsonProvider.class);
        register(MultiPartFeature.class);
        register(InformasjonRessurs.class);
        register(TestController.class);
        register(TjenesterRessurs.class);
        register(VedleggRessurs.class);
        register(SoknadRessurs.class);
        register(FaktaRessurs.class);
        register(SoknadObjectMapperProvider.class);
        register(JsonToTextPlainBodyWriter.class);
        register(ThrowableMapper.class);
        register(ApplicationExceptionMapper.class);
        register(SoknadActions.class);

        logger.info("Starter Jersey#########################################################");
    }
}