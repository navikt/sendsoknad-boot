package no.nav.modig.core.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PropertySetter {

    private static final Logger LOG = LoggerFactory.getLogger(PropertySetter.class);

    private final Properties propertiesToSet;

    public PropertySetter(Properties propertiesToSet) {
        this.propertiesToSet = propertiesToSet;
    }

    public final void setOn(Properties properties) {
        for (String propName : propertiesToSet.stringPropertyNames()) {
            if (properties.containsKey(propName)) {
                LOG.warn("Overwriting {} = {} to {} = {}",
                        propName, properties.getProperty(propName), propName, propertiesToSet.getProperty(propName));
            } else {
                LOG.info("Setting {} = {}", propName, propertiesToSet.getProperty(propName));
            }
            properties.setProperty(propName, propertiesToSet.getProperty(propName));
        }
    }

    public static class PropertySetterTester extends PropertySetter {

        public PropertySetterTester() {
            super(createKeyStoreProperties());
        }

        private static Properties createKeyStoreProperties() {
            Properties p = new Properties();
            p.setProperty("key1", "value1");
            p.setProperty("key2", "value2");
            return p;
        }

        public void setProperties(PropertySetter ps, String key1, String value1, String key2, String value2) {
            Properties p = (Properties) System.getProperties().clone();

            assertNotEquals(value1, p.getProperty(key1));
            assertNotEquals(value2, p.getProperty(key2));
            ps.setOn(p);
            assertEquals(value1, p.getProperty(key1));
            assertEquals(value2, p.getProperty(key2));
        }

        public void overwriteProperties(PropertySetter ps, String key1, String value1, String key2, String value2) {
            Properties p = (Properties) System.getProperties().clone();
            p.setProperty(key1, "verdi");
            p.setProperty(key2, "verdi");

            assertNotEquals(value1, p.getProperty(key1));
            assertNotEquals(value2, p.getProperty(key2));
            ps.setOn(p);
            assertEquals(value1, p.getProperty(key1));
            assertEquals(value2, p.getProperty(key2));
        }
    }
}
