package no.nav.sbl.dialogarena.sikkerhet;

import static org.junit.Assert.fail;

import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;

public class XsrfGeneratorTest {

    //@TODO XSRF sjekken kan forbedres. XSRF generatoren er ikke helt OK etter OpenAM migrasjonen. Np bruker den bare behandlingsid
    @Test
    @Ignore
    public void skalGenerereBasertPaaInput() {
       // setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        String token = XsrfGenerator.generateXsrfToken("1L");
        String tokenYesterday = XsrfGenerator.generateXsrfToken("1L", new DateTime().minusDays(1).toString("yyyyMMdd"));
        XsrfGenerator.sjekkXsrfToken(token, "1L");
        XsrfGenerator.sjekkXsrfToken(tokenYesterday, "1L");
        sjekkAtMetodeKasterException(token, 2L);
       // ((StaticSubjectHandler) StaticSubjectHandler.getSubjectHandler()).setSubject(newSubject());
        sjekkAtMetodeKasterException(token, 1L);
       // ((StaticSubjectHandler) StaticSubjectHandler.getSubjectHandler()).reset();
    }

    private void sjekkAtMetodeKasterException(String token, long soknadId) {
        try {
            XsrfGenerator.sjekkXsrfToken(token, String.valueOf(soknadId));
            fail("Kastet ikke exception");
        } catch (AuthorizationException ex) {
            // Should throw exception
        }
    }
/*
    private Subject newSubject() {
        Subject subject = new Subject();
        subject.getPrincipals().add(SluttBruker.eksternBruker("98989898989"));
        subject.getPrincipals().add(new ConsumerId("StaticSubjectHandlerConsumerId"));
        subject.getPublicCredentials().add(new OpenAmTokenCredential("98989898989-4"));
        subject.getPublicCredentials().add(new AuthenticationLevelCredential(4));
        return subject;
    }
    */
}
