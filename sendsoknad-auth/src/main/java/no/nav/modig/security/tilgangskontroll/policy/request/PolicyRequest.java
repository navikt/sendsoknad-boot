package no.nav.modig.security.tilgangskontroll.policy.request;

import no.nav.modig.security.tilgangskontroll.policy.request.attributes.PolicyAttribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;

/**
 * XACML policy request.
 *
 * Contains a list of attributes describing subject, resource, action and environment.
 * Is used together with {@link no.nav.modig.security.tilgangskontroll.policy.pep.EnforcementPoint}
 * to check if subject is authenticated to perform a given action on a resource in
 * the current environment.
 */
public final class PolicyRequest implements Serializable {

    private static final long serialVersionUID = -2483010602793085487L;

    private final Collection<PolicyAttribute> attributes;

    public PolicyRequest(Collection<PolicyAttribute> attributes) {
        this.attributes = unmodifiableCollection(attributes);
    }

    public PolicyRequest(PolicyAttribute... attributes) {
        this.attributes = unmodifiableCollection(asList(attributes));
    }

    /**
     * Returns a read only copy of the attribute list
     *
     * @return Read only collection of attributes
     */
    public Collection<PolicyAttribute> getAttributes() {
        return attributes;
    }

    /**
     * Creates a copy of the request with additional attributes appended.
     *
     * This leaves the original request untouched.
     *
     * @param additionalAttributes additional attributes
     * @return copy of request
     */
    public PolicyRequest copyAndAppend(PolicyAttribute... additionalAttributes) {
        List<PolicyAttribute> attrs = new ArrayList<>();
        attrs.addAll(attributes);
        attrs.addAll(asList(additionalAttributes));
        return new PolicyRequest(attrs);
    }

    /**
     * Creates a copy of the request with all attributes resolved.
     *
     * @return copy of request with resolved attributes.
     */
    public PolicyRequest copyAndResolveAttributeValues() {
        List<PolicyAttribute> resolvedAttributes = attributes.stream()
                .map(PolicyAttribute::resolvedCopy)
                .collect(Collectors.toList());
        return new PolicyRequest(resolvedAttributes);
    }

	/**
	 * @return  logg-vennlig utskrift av request
	 */
	public String toLogString() {
        String logMessage = attributes.stream()
                .map(PolicyAttribute::toLogString)
                .collect(Collectors.joining(", "));
		return "PolicyRequest {Used attributes: " + logMessage + '}';
	}

    @Override
    public String toString() {
        return "PolicyRequest{attributes=" + attributes + '}';
    }
}
