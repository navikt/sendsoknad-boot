package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

import java.io.Serializable;

/**
 * An {@link AttributeValue} might resolve at a point later than construction time. Use a {@link AttributeValueResolver} if the
 * value is to be resolved first when a {@link no.nav.modig.security.tilgangskontroll.policy.request.PolicyRequest} is created.
 * 
 * This is useful when binding an attribute value to the value of wicket models.
 */
public interface AttributeValueResolver<T> extends Serializable {

    T getValue();
}
