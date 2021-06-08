package no.nav.sbl.dialogarena.soknadinnsending.consumer;


import org.junit.Test;

public class ServiceBuilderTest {
	
	private static interface TestInterface {} 
	
    @Test
    public void skalByggeTjeneste(){
        new ServiceBuilder<>(TestInterface.class)
                .asStandardService()
                .withTimeout()
                .withAddressing()
                .withLogging()
                .withProperties()
                .withExtraClasses(new Class[]{String.class})
                .build()
                .withMDC().get();
    }
}
