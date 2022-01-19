package no.nav.modig.security.tilgangskontroll.policy.enrichers;

import static no.nav.modig.security.tilgangskontroll.policy.request.attributes.SubjectAttribute.ACCESS_SUBJECT;
import static no.nav.modig.security.tilgangskontroll.utils.AttributeUtils.authenticationLevel;
import static no.nav.modig.security.tilgangskontroll.utils.AttributeUtils.identType;
import static no.nav.modig.security.tilgangskontroll.utils.AttributeUtils.subjectId;

import org.apache.commons.lang3.StringUtils;

import no.nav.modig.core.domain.IdentType;
import no.nav.modig.security.tilgangskontroll.policy.request.PolicyRequest;
import no.nav.sbl.dialogarena.tokensupport.TokenUtils;

public class SecurityContextRequestEnricher implements PolicyRequestEnricher {

    @Override
    public PolicyRequest enrich(PolicyRequest request) {

       
        

        if (StringUtils.isEmpty(TokenUtils.getSubject())) {
            return request;
        }

        String uid = TokenUtils.getSubject();
        String authLevel =  "Level4";
        String identType = IdentType.EksternBruker.toString();
       

        return request.copyAndAppend(
                subjectId(ACCESS_SUBJECT, uid),
                authenticationLevel(ACCESS_SUBJECT, authLevel),
                identType(ACCESS_SUBJECT, identType));
    }

}
