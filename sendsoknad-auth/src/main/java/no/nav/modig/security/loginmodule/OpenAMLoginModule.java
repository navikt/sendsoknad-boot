package no.nav.modig.security.loginmodule;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import no.nav.modig.core.context.AuthenticationLevelCredential;
import no.nav.modig.core.context.OpenAmTokenCredential;
import no.nav.modig.core.domain.ConsumerId;
import no.nav.modig.core.domain.SluttBruker;
import no.nav.modig.security.loginmodule.userinfo.UserInfo;
import no.nav.modig.security.loginmodule.userinfo.UserInfoService;
import no.nav.modig.security.loginmodule.userinfo.openam.OpenAMUserInfoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenAMLoginModule implements LoginModule {

	private static final Logger logger = LoggerFactory.getLogger(OpenAMLoginModule.class);
	private static final String OPTION_USERINFOSERVICE_URL = "userinfoservice.url";
	private static final String OPTION_TRUSTSTORE_FILE="userinfoservice.truststore.file";
	private static final String OPTION_TRUSTSTORE_PASSWORD="userinfoservice.truststore.password";
	private static final String[] REQUIRED_OPTIONS = {OPTION_USERINFOSERVICE_URL};

    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String, ?> options;
    private String uid;
    private int authLevel;
    private String ssoToken;
    private UserInfoService userInfoService;
    private boolean loginSuccess = false;
    private ConsumerId consumerId;

    private File trustStoreFile = null; //If null the UserInfoService will use the default truststore
    private String truststorePassword = null;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
    	logger.info("Initialize loginmodule.");
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.options = options;
        validateRequiredOptions();

        this.trustStoreFile = resolveOption(OPTION_TRUSTSTORE_FILE) != null ? new File(resolveOption(OPTION_TRUSTSTORE_FILE)) : null;
        this.truststorePassword = resolveOption(OPTION_TRUSTSTORE_PASSWORD);
        this.userInfoService = createUserInfoService();
        logger.info("Initializing with subject: " + subject +
                " callbackhandler: " + callbackHandler +
                " and userinfoservice:  " + userInfoService);
    }

    @Override
    public boolean login() throws LoginException {

        logger.info("Enter login method");
        ssoToken = getSSOToken();

        UserInfo userInfo = userInfoService.getUserInfo(ssoToken);

        loginSuccess = true;

        authLevel = userInfo.getAuthLevel();
        uid = userInfo.getUid();

        consumerId = new ConsumerId();

        logger.info("Login successful for user " + "FNR" + " with authentication level " + authLevel);

        return true;

    }

    protected UserInfoService createUserInfoService() {
    	//Should already been validated as non-null.
    	String endpoint = resolveOption(OPTION_USERINFOSERVICE_URL);
    	URI uri = URI.create(endpoint);
    	String scheme = uri.getScheme();
    	if(scheme == null){
    		throw new IllegalArgumentException("Loginmodule option "+OPTION_USERINFOSERVICE_URL + " must resolve to valid URL. Resolves to " + endpoint);
    	} else {
    		return new OpenAMUserInfoService(uri, trustStoreFile, truststorePassword);
    	}

    }

    @Override
    public boolean commit() throws LoginException {

        logger.info("Enter commit method");
        if(!subject.isReadOnly()){
	        if (!loginSuccess) {
	            uid = null;
	            authLevel = -1;
	            ssoToken = null;

	            logger.info("Commit failed because login was unsuccessful");
	            throw new LoginException("Login failed, cannot commit");
	        }
	        
	        subject.getPrincipals().add(SluttBruker.eksternBruker(uid));
	        subject.getPublicCredentials().add(new AuthenticationLevelCredential(authLevel));
	        subject.getPublicCredentials().add(new OpenAmTokenCredential(ssoToken));
            subject.getPrincipals().add(consumerId);


            logger.info("Login committed for subject with uid: " + "FNR" +
	            " authentication level: " + authLevel +
	            " and credential: " + "ssoToken" + " and serviceUser: " + consumerId);

	        return true;
        } else {
        	throw new LoginException("Commit failed. Subject is read-only. Cannot add principals and credentials.");
        }

    }

    @Override
    public boolean abort() throws LoginException {
    	logger.info("Enter abort method");
		uid = null;
		authLevel = -1;
		ssoToken = null;
        consumerId = null;

		if(!subject.isReadOnly()){
        	cleanUpSubject();
        }
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        logger.info("Enter logout method");
        if(!subject.isReadOnly()){
        	cleanUpSubject();
        	return true;
        } else {
        	logger.info("Subject is readonly, cannot cleanup subject.");
        	return false;
        }
    }

    private void cleanUpSubject() throws LoginException {
    	Set<DestroyFailedException> exceptions = new HashSet<>();

        Set<SluttBruker> principals = subject.getPrincipals(SluttBruker.class);
        for (SluttBruker ebp : principals) {
            try {
                String msg = "Logout destroyed and removed " + ebp;
                ebp.destroy();
                subject.getPrincipals().remove(ebp);
                logger.info(msg);
            } catch (DestroyFailedException e) {
                exceptions.add(e);
            }
        }

        Set<ConsumerId> consumerIdPrincipals = subject.getPrincipals(ConsumerId.class);
        for (ConsumerId cip: consumerIdPrincipals) {
            try {
                String msg = "Logout destroyed and removed " + cip;
                cip.destroy();
                subject.getPrincipals().remove(cip);
                logger.info(msg);
            } catch (DestroyFailedException e) {
                exceptions.add(e);
            }
        }


        Set<OpenAmTokenCredential> openAmTokenCredentials = subject.getPublicCredentials(OpenAmTokenCredential.class);
        for (OpenAmTokenCredential openAmTokenCredential : openAmTokenCredentials) {
            try {
                String msg = "Logout destroyed and removed " + openAmTokenCredential;
                openAmTokenCredential.destroy();
                subject.getPublicCredentials().remove(openAmTokenCredential);
                logger.info(msg);

            } catch (DestroyFailedException e) {
                exceptions.add(e);
            }
        }

        Set<AuthenticationLevelCredential> authenticationLevelCredentials = subject.getPublicCredentials(AuthenticationLevelCredential.class);
        for (AuthenticationLevelCredential authenticationLevelCredential : authenticationLevelCredentials) {
            try {
                String msg = "Logout destroyed and removed " + authenticationLevelCredential;
                authenticationLevelCredential.destroy();
                subject.getPublicCredentials().remove(authenticationLevelCredential);
                logger.info(msg);

            } catch (DestroyFailedException e) {
                exceptions.add(e);
            }
        }

        if (!exceptions.isEmpty()) {
            logger.info("Logout failed: " + exceptions);
            throw new LoginException("Failed to destroy principals and/or credentials: " + exceptions);

        }
    }

    /**
     * Called by login() to acquire the OpenAMToken.
     */
    protected String getSSOToken() throws LoginException
    {
        logger.info("Getting the SSO-token from callback");

        if (callbackHandler == null)
        {
            LoginException le = new LoginException("No callbackhandler provided");
            throw le;
        }

        // The prompt will never be seen by the user, we trigger the module by code with username set programatically
        NameCallback nc = new NameCallback("Input SSO token");

        Callback[] callbacks = { nc };

        String tokenString = null;
        try
        {
            callbackHandler.handle(callbacks);
            tokenString = nc.getName();
        } catch (IOException e)
        {
            logger.info("Error while handling getting token from callbackhandler: "  + e);
            LoginException le = new LoginException();
            le.initCause(e);
            throw le;
        } catch (UnsupportedCallbackException e)
        {
            logger.info("Error while handling getting token from callbackhandler: "  + e);
            LoginException le = new LoginException();
            le.initCause(e);
            throw le;
        }
        return tokenString;
    }

    private void validateRequiredOptions(){
		for (String key: REQUIRED_OPTIONS) {
			if(resolveOption(key) == null){
				throw new IllegalArgumentException("Could not resolve required option "+key+" in loginmodule configuration.");
			}
		}
    }

    /**
     * Returns the value of an option configured for this loginmodule. If value of option resolves to a System.getProperty(value),
     * return System.getProperty(value) otherwise return the value of the option. Returns null if not found.
     *
     * @param key the key
     * @return If value of option resolves to a System.getProperty(value), return System.getProperty(value) otherwise return the value of the option. Returns null if not found.
     */
    private String resolveOption(String key){
    	String value = (String)options.get(key);
    	String systemProperty = value != null ? System.getProperty(value) : null;
    	return systemProperty != null ? systemProperty : value;
    }
}
