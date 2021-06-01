package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.*;
import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.UUID;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoTransformers.extractValue;

public class TilleggsstonaderTilXml implements AlternativRepresentasjonTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(TilleggsstonaderTilXml.class);
    private final TekstHenter tekstHenter;

    public TilleggsstonaderTilXml(TekstHenter tekstHenter) {
        this.tekstHenter = tekstHenter;
    }

    private static Tilleggsstoenadsskjema tilTilleggsstoenadSkjema(WebSoknad webSoknad, TekstHenter tekstHenter) {
        Tilleggsstoenadsskjema skjema = new Tilleggsstoenadsskjema();
        skjema.setMaalgruppeinformasjon(MaalgruppeTilXml.transform(webSoknad.getFaktumMedKey("maalgruppe")));
        Rettighetstype rettighetstype = new Rettighetstype();

        if (aktivBolk("bostotte", webSoknad)) {
            rettighetstype.setBoutgifter(BoutgifterTilXml.transform(webSoknad));
        }
        if (aktivBolk("laeremidler", webSoknad)) {
            rettighetstype.setLaeremiddelutgifter(LaeremidlerTilXml.transform(webSoknad));
        }
        if (aktivBolk("flytting", webSoknad)) {
            rettighetstype.setFlytteutgifter(FlytteutgifterTilXml.transform(webSoknad, tekstHenter));
        }
        rettighetstype.setTilsynsutgifter(tilsynsutgifter(webSoknad, tekstHenter));
        rettighetstype.setReiseutgifter(reiseutgifter(webSoknad));

        skjema.setRettighetstype(rettighetstype);
        skjema.setAktivitetsinformasjon(aktivitetsInformasjon(webSoknad));
        skjema.setPersonidentifikator(webSoknad.getAktoerId());
        return skjema;
    }

    private static Tilsynsutgifter tilsynsutgifter(WebSoknad webSoknad, TekstHenter tekstHenter) {
        Tilsynsutgifter tilsynsutgifter = new Tilsynsutgifter();
        if (aktivBolk("familie", webSoknad)) {
            tilsynsutgifter.setTilsynsutgifterFamilie(TilsynFamilieTilXml.transform(webSoknad));
        }
        if (aktivBolk("barnepass", webSoknad)) {
            tilsynsutgifter.setTilsynsutgifterBarn(TilsynBarnepassTilXml.transform(webSoknad, tekstHenter));
        }

        return tilsynsutgifter.getTilsynsutgifterBarn() == null && tilsynsutgifter.getTilsynsutgifterFamilie() == null ? null : tilsynsutgifter;
    }

    private static Aktivitetsinformasjon aktivitetsInformasjon(WebSoknad webSoknad) {
        Aktivitetsinformasjon result = new Aktivitetsinformasjon();
        String value = extractValue(webSoknad.getFaktumMedKey("aktivitet"), String.class, "id");
        if (Arrays.asList(null, "", "ikkeaktuelt", "arbeidssoking").contains(value)) {
            return null;
        }
        result.setAktivitetsId(value);
        return result;
    }

    private static boolean aktivBolk(String bolk, WebSoknad webSoknad) {
        Faktum bolkFaktum = webSoknad.getFaktumMedKey("informasjonsside.stonad." + bolk);
        return bolkFaktum != null && "true".equals(bolkFaktum.getValue());
    }

    private static Reiseutgifter reiseutgifter(WebSoknad webSoknad) {
        Reiseutgifter reiseutgifter = new Reiseutgifter();
        boolean satt = false;
        if (aktivBolk("reiseaktivitet", webSoknad)) {
            reiseutgifter.setDagligReise(DagligReiseTilXml.transform(webSoknad));
            satt = true;
        }
        if (aktivBolk("reisearbeidssoker", webSoknad)) {
            reiseutgifter.setReisestoenadForArbeidssoeker(ArbeidReiseTilXml.transform(webSoknad));
            satt = true;
        }
        if (aktivBolk("reisemidlertidig", webSoknad)) {
            reiseutgifter.setReiseVedOppstartOgAvsluttetAktivitet(ReiseOppstartOgAvsluttetAktivitetTilXml.transform(webSoknad));
            satt = true;
        }
        if (aktivBolk("reisesamling", webSoknad)) {
            reiseutgifter.setReiseObligatoriskSamling(SamlingReiseTilXml.transform(webSoknad));
            satt = true;
        }
        if (satt) {
            return reiseutgifter;
        }
        return null;
    }

    @Override
    public AlternativRepresentasjon apply(WebSoknad webSoknad) {
        return transform(webSoknad);
    }

    public AlternativRepresentasjon transform(WebSoknad webSoknad) {
        Tilleggsstoenadsskjema tilleggsstoenadsskjema = tilTilleggsstoenadSkjema(webSoknad, tekstHenter);
        validerSkjema(tilleggsstoenadsskjema, webSoknad);
        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        JAXB.marshal(tilleggsstoenadsskjema, xml);
        return new AlternativRepresentasjon()
                .medRepresentasjonsType(getRepresentasjonsType())
                .medMimetype("application/xml")
                .medFilnavn("Tilleggsstonader.xml")
                .medUuid(UUID.randomUUID().toString())
                .medContent(xml.toByteArray());
    }

    private void validerSkjema(Tilleggsstoenadsskjema tilleggsstoenadsskjema, WebSoknad soknad) {
        QName qname = new QName("http://nav.no/melding/virksomhet/soeknadsskjema/v1/soeknadsskjema", "tilleggsstoenadsskjema");
        JAXBElement<Tilleggsstoenadsskjema> skjema = new JAXBElement<>(qname, Tilleggsstoenadsskjema.class, tilleggsstoenadsskjema);
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(TilleggsstonaderTilXml.class.getResourceAsStream("/xsd/no/nav/melding/virksomhet/soeknadsskjema/v1/soeknadsskjema/soeknadsskjema.xsd")));
            Marshaller m = JAXBContext.newInstance(Tilleggsstoenadsskjema.class).createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.setSchema(schema);
            Validator validator = schema.newValidator();
            JAXBSource source = new JAXBSource(m, skjema);
            validator.validate(source);
        } catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JAXB.marshal(skjema, baos);
            LOG.warn("Validering av skjema feilet: " + e + ". Xml: " + baos.toString(), e);
            Event event = MetricsFactory.createEvent("soknad.xmlrepresentasjon.valideringsfeil");
            event.addTagToReport("soknad.xmlrepresentasjon.valideringsfeil.skjemanummer", soknad.getskjemaNummer());
            event.report();
        }
    }

    @Override
    public AlternativRepresentasjonType getRepresentasjonsType() {
        return AlternativRepresentasjonType.XML;
    }
}
