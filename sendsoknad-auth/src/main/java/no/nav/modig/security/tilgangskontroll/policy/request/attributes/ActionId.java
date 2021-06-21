package no.nav.modig.security.tilgangskontroll.policy.request.attributes;

import no.nav.modig.security.tilgangskontroll.policy.attributes.AttributeIds;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.StringValue;

/**
 * Action-attributt
 */
public class ActionId extends ActionAttribute {
    public static final ActionId READ = new ActionId(new StringValue("read"));
    public static final ActionId EDIT = new ActionId(new StringValue("edit"));
    public static final ActionId VIEW = new ActionId(new StringValue("view"));

    public ActionId(StringValue attributeValue) {
        super(AttributeIds.ACTION_ID, attributeValue);
    }
}
