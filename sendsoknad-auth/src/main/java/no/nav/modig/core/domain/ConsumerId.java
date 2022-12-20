package no.nav.modig.core.domain;

import javax.security.auth.Destroyable;
import java.security.Principal;

public final class ConsumerId implements Principal, Destroyable {

    private String consumerIdString;
    private boolean destroyed;

    private static final String SYSTEM_USER = "systemuser.sendsoknad.username";

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
    public void destroy() {
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
        return getClass().getSimpleName() + "[" +
                (destroyed ? "destroyed" : consumerIdString) +
                "]";
    }
}
