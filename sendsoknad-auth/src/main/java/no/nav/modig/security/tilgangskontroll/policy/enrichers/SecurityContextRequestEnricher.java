package no.nav.modig.security.tilgangskontroll.policy.enrichers;

import static no.nav.modig.security.tilgangskontroll.policy.request.attributes.SubjectAttribute.ACCESS_SUBJECT;
import static no.nav.modig.security.tilgangskontroll.policy.request.attributes.SubjectAttribute.CONSUMER;
import static no.nav.modig.security.tilgangskontroll.utils.AttributeUtils.authenticationLevel;
import static no.nav.modig.security.tilgangskontroll.utils.AttributeUtils.identType;
import static no.nav.modig.security.tilgangskontroll.utils.AttributeUtils.subjectId;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.security.tilgangskontroll.policy.request.PolicyRequest;

public class SecurityContextRequestEnricher implements PolicyRequestEnricher {

    @Override
    public PolicyRequest enrich(PolicyRequest request) {

        SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();

        if (subjectHandler.getUid() == null) {
            return request;
        }

        String uid = subjectHandler.getUid();
        String authLevel = subjectHandler.getAuthenticationLevel() != null ? subjectHandler.getAuthenticationLevel().toString() : null;
        String identType = subjectHandler.getIdentType() != null ? subjectHandler.getIdentType().toString() : null;
        String consumerId = subjectHandler.getConsumerId();

        return request.copyAndAppend(
                subjectId(ACCESS_SUBJECT, uid),
                authenticationLevel(ACCESS_SUBJECT, authLevel),
                identType(ACCESS_SUBJECT, identType),
                subjectId(CONSUMER, consumerId));
    }

}
