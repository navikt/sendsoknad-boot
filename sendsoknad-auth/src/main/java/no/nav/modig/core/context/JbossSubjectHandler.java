package no.nav.modig.core.context;

import javax.security.auth.Subject;

import org.jboss.security.SecurityContextAssociation;

public class JbossSubjectHandler extends SubjectHandler {

    @Override
    public Subject getSubject() {
        return SecurityContextAssociation.getSubject();
    }

}
