package no.nav.modig.core.domain;

import no.nav.modig.core.context.ModigSecurityConstants;

import java.security.Principal;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

public final class ConsumerId implements Principal, Destroyable {

    private String consumerIdString;
    private boolean destroyed;
    
    private static String SYSTEM_USER = "systemuser.sendsoknad.username";

    public ConsumerId(String consumerId) {
        this.consumerIdString = consumerId;
    }

    public ConsumerId() {
        consumerIdString = System.getProperty(SYSTEM_USER);

        if(consumerIdString == null){
            throw new IllegalStateException(
                    SYSTEM_USER + " is not set, failed to set "+ ConsumerId.class.getName());
        }
    }

    @Override
    public void destroy() throws DestroyFailedException {
        consumerIdString = null;
        destroyed = true;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public String getName() {
        return consumerIdString;
    }

    public String getConsumerId() {
        return consumerIdString;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append("[")
                        .append(destroyed ? "destroyed" : consumerIdString)
                        .append("]");
        return sb.toString();
    }
}
