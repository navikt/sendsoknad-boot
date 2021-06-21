package no.nav.modig.security.tilgangskontroll.policy.request;

import no.nav.modig.security.tilgangskontroll.policy.request.attributes.PolicyAttribute;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.modig.security.tilgangskontroll.policy.request.attributes.PolicyAttribute.AS_RESOLVED;
import static org.apache.commons.collections4.CollectionUtils.collect;
import static org.apache.commons.collections4.CollectionUtils.unmodifiableCollection;

/**
 * XACML policy request.
 * <p/>
 * Contains a list of attributes describing subject, resource, action and environment. Is used together with
 * {@link no.nav.modig.security.tilgangskontroll.policy.pep.EnforcementPoint} to check if subject is authenticated to perform a
 * given action on a resource in the current environment.
 */
public final class PolicyRequest implements Serializable {

    /**
     * serialVersionUID
     */
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
        return unmodifiableCollection(attributes);
    }

    /**
     * Creates a copy of the request with additional attributes appended.
     * <p/>
     * This leaves the original request untouched.
     * 
     * @param additionalAttributes
     *            additional attributes
     * @return copy of request
     */
    public PolicyRequest copyAndAppend(PolicyAttribute... additionalAttributes) {
        List<PolicyAttribute> attrs = new ArrayList<PolicyAttribute>();
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
        return new PolicyRequest(collect(attributes, AS_RESOLVED));
    }

	/**
	 *
	 * @return  logg-vennlig utskrift av request
	 */
	public String toLogString() {
		List<String> l = new ArrayList<String>();
		for (PolicyAttribute attribute : attributes) {
			l.add(attribute.toLogString());
		}
		String logMessage = StringUtils.join(l, ", ");

		return "PolicyRequest {"
				+
				"Used attributes: " + logMessage
				+ '}';
	}

    @Override
    public String toString() {
        return "PolicyRequest{"
                +
                "attributes=" + attributes
                + '}';
    }

}