package no.nav.modig.security.tilgangskontroll.policy.pdp.picketlink;

import no.nav.modig.security.tilgangskontroll.URN;
import no.nav.modig.security.tilgangskontroll.policy.request.PolicyRequest;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.ActionAttribute;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.PolicyAttribute;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.ResourceAttribute;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.SubjectAttribute;
import org.jboss.security.xacml.core.JBossRequestContext;
import org.jboss.security.xacml.core.model.context.*;
import org.jboss.security.xacml.interfaces.RequestContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Internal factory.
 *
 * Creates JBoss XACML {@link RequestType} and {@link RequestContext} from {@link PolicyRequest}
 *
 * Should not be exposed.
 */
final class RequestTypeFactory {

    public static RequestContext createXacmlRequest(PolicyRequest request) {
        RequestContext requestContext = new JBossRequestContext();
        try {
            requestContext.setRequest(createRequestType(request));
        } catch (IOException e) {
            throw new RuntimeException("Unable to create XACML request", e);
        }
        return requestContext;
    }

    public static RequestType createRequestType(PolicyRequest policyRequest) {
        return new RequestTypeBuilder(policyRequest).build();
    }

    public static class RequestTypeBuilder {
        private final PolicyRequest policyRequest;

        public RequestTypeBuilder(PolicyRequest policyRequest) {
            this.policyRequest = policyRequest;
        }

        public RequestType build() {
            RequestType requestType = new RequestType();
            requestType.getSubject().addAll(createSubjectTypes());
            requestType.getResource().add(createResourceType());
            requestType.setAction(createActionType());
            return requestType;
        }

        private ActionType createActionType() {
            ActionType actionType = new ActionType();
            for (ActionAttribute actionAttribute : getActionAttributes()) {
                actionType.getAttribute().add(createAttributeType(actionAttribute));
            }
            return actionType;
        }

        private ResourceType createResourceType() {
            ResourceType resourceType = new ResourceType();
            for (ResourceAttribute resourceAttribute : getResourceAttributes()) {
                resourceType.getAttribute().add(createAttributeType(resourceAttribute));
            }
            return resourceType;
        }

        private List<SubjectType> createSubjectTypes() {
            Collection<URN> subjectCategories = getSubjectAttributes().stream()
                    .map(SubjectAttribute::getSubjectCategory)
                    .collect(Collectors.toSet());

            List<SubjectType> subjects = new ArrayList<>();
            for (URN subjectCategory : subjectCategories) {
                SubjectType subjectType = new SubjectType();
                subjectType.setSubjectCategory(subjectCategory.getURN());

                Collection<SubjectAttribute> attributes = getSubjectAttributes().stream()
                        .filter(subjectAttribute -> subjectAttribute.getSubjectCategory().equals(subjectCategory))
                        .collect(Collectors.toList());
                for (SubjectAttribute subjectAttribute : attributes) {
                    subjectType.getAttribute().add(createAttributeType(subjectAttribute));
                }
                subjects.add(subjectType);
            }

            return subjects;
        }

        private List<SubjectAttribute> getSubjectAttributes() {
            ArrayList<SubjectAttribute> subjectAttributes = new ArrayList<>();
            for (PolicyAttribute attribute : policyRequest.getAttributes()) {
                if (attribute instanceof SubjectAttribute) {
                    subjectAttributes.add((SubjectAttribute) attribute);
                }
            }
            return subjectAttributes;
        }

        private List<ActionAttribute> getActionAttributes() {
            ArrayList<ActionAttribute> actionAttributes = new ArrayList<>();
            for (PolicyAttribute attribute : policyRequest.getAttributes()) {
                if (attribute instanceof ActionAttribute) {
                    actionAttributes.add((ActionAttribute) attribute);
                }
            }
            return actionAttributes;
        }

        private List<ResourceAttribute> getResourceAttributes() {
            ArrayList<ResourceAttribute> resourceAttributes = new ArrayList<>();
            for (PolicyAttribute attribute : policyRequest.getAttributes()) {
                if (attribute instanceof ResourceAttribute) {
                    resourceAttributes.add((ResourceAttribute) attribute);
                }
            }
            return resourceAttributes;
        }

        private static AttributeType createAttributeType(PolicyAttribute attribute) {
            return AttributeConverter.convert(attribute);
        }
    }
}
