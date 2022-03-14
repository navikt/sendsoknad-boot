package no.nav.sbl.sendsoknad;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SystemPropertiesInitializer implements InitializingBean {
        
        @Autowired
        @Qualifier("kravdialoginformasjon")
        Map<String,String> kravinformasjonMap; 
        
        @Value("${systemuser.sendsoknad.username}")
        private String username;
        
        @Value("${systemuser.sendsoknad.password}")
        private String password;

        @Autowired
        Environment env;
        
        @Override
        public void afterPropertiesSet() throws Exception {
                
                for (Entry<String, String> entry: kravinformasjonMap.entrySet()) {
                        System.setProperty(entry.getKey(), entry.getValue());
                }
                //System.set
                
                System.setProperty("systemuser.sendsoknad.username",username);
                System.setProperty("systemuser.sendsoknad.password",password);
                
        }

        
        
}
