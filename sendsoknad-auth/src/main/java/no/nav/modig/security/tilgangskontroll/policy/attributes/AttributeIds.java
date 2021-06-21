package no.nav.modig.security.tilgangskontroll.policy.attributes;

import no.nav.modig.security.tilgangskontroll.URN;

/**
 * A collection of common attributeIds.
 */
public final class AttributeIds {

    public static final String NAV_XACML_URN_ROOT = "urn:nav:ikt:tilgangskontroll:xacml:";

    // Subject attributes
    public static final String ATTR_SUBJECT_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
    public static final URN SUBJECT_ID = new URN(ATTR_SUBJECT_ID);

    public static final URN AUTHENTICATION_LEVEL = navUrn("subject:authentication-level");
    public static final URN IDENT_TYPE = navUrn("subject:ident-type");

    public static final String ATTR_ACCESS_SUBJECT = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
    public static final URN ACCESS_SUBJECT = new URN(ATTR_ACCESS_SUBJECT);

    // Action attributes
    public static final String ATTR_ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
    public static final URN ACTION_ID = new URN(ATTR_ACTION_ID);

    public static final URN COMPONENT_ACTION_NAME = navUrn("action:component-action-name");

    // Resource attributes
    public static final String ATTR_RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
    public static final URN RESOURCE_ID = new URN(ATTR_RESOURCE_ID);
    public static final String ATTR_COMPONENT_ID = NAV_XACML_URN_ROOT + "resource:component-id";
    public static final URN COMPONENT_ID = new URN(ATTR_COMPONENT_ID);

    public static final URN RESOURCE_TYPE = navUrn("resource:resource-type");
    public static final URN COMPONENT_TYPE = navUrn("resource:component-type");

    public static final URN PAGE_TYPE = navUrn("resource:page-type");
    public static final URN OWNER_ID = navUrn("resource:owner-id");
    public static final URN FODSELS_NUMMER = navUrn("resource:person:fodselsnummer");

    // Environment attributes
    public static final URN CURRENT_TIME = new URN("urn:oasis:names:tc:xacml:1.0:environment:current-time");
    public static final URN CURRENT_DATE = new URN("urn:oasis:names:tc:xacml:1.0:environment:current-date");
    public static final URN CURRENT_DATE_TIME = new URN("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime");

    private AttributeIds() {
    }

    static {
        new AttributeIds();
    }

    private static URN navUrn(String relativeUrn) {
        return new URN(NAV_XACML_URN_ROOT + relativeUrn);
    }
}
