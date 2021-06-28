package no.nav.sbl.dialogarena.integration;


import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPUtlandetInformasjon;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

public class AAPUtlandVedleggIT extends AbstractIT {
    private String skjemanummer = new AAPUtlandetInformasjon().getSkjemanummer().get(0);

    @MockBean
    DataSource datasource;
    
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
