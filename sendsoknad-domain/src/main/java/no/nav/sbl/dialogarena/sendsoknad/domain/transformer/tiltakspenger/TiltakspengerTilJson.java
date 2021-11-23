package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tiltakspenger;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;

public class TiltakspengerTilJson implements AlternativRepresentasjonTransformer {
    @Override
    public AlternativRepresentasjonType getRepresentasjonsType() {
        return AlternativRepresentasjonType.JSON;
    }

    @Override
    public AlternativRepresentasjon apply(WebSoknad webSoknad) {
        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        //JAXB.marshal(xml);

        return new AlternativRepresentasjon()
                .medMimetype("application/json")
                .medFilnavn("Tiltakspenger.json")
                .medContent(new byte[0]);
    }
}
