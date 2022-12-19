package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPUtlandetInformasjon;
import no.nav.sbl.dialogarena.utils.TestTokenUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.sql.DataSource;

public class AAPUtlandVedleggIT extends AbstractIT {
    private final String skjemanummer = new AAPUtlandetInformasjon().getSkjemanummer().get(0);

    @MockBean
    DataSource datasource;


    @BeforeClass
    public static void initializeTokenValidationContext() throws Exception {
       TestTokenUtils.setSecurityContext();
    }

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void skalIkkeKreveNoenVedleggForKomplettSoknad() {
        soknadMedDelstegstatusOpprettet(skjemanummer)
                .faktum("land").withValue("NOR").utforEndring()
                .faktum("fradato").withValue("01-01-2018").utforEndring()
                .faktum("tildato").withValue("01-02-2018").utforEndring()
                .faktum("oppholdikkehinder").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalIkkeKreveNoenVedlegg();
    }
}
