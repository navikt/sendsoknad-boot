package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;

public class StaticMetoder {

    public static String skjemanummer(WebSoknad soknad) {
        return soknad.getskjemaNummer();
    }

    public static String journalforendeEnhet(WebSoknad soknad) {
        return soknad.getJournalforendeEnhet();
    }

    public static Predicate<XMLMetadata> IKKE_KVITTERING = xmlMetadata ->
            !(xmlMetadata instanceof XMLVedlegg && SKJEMANUMMER_KVITTERING.equals(((XMLVedlegg) xmlMetadata).getSkjemanummer()));

    public static DateTime hentOrginalInnsendtDato(List<WSBehandlingskjedeElement> behandlingskjede, String behandlingsId) {
        return behandlingskjede.stream()
                .filter(element-> element.getBehandlingsId().equals(behandlingsId))
                .findFirst()
                .get()
                .getInnsendtDato();
    }

    public static final Comparator<WSBehandlingskjedeElement> ELDSTE_FORST = (o1, o2) -> sammenlignBehandlingBasertPaaDato(o1, o2);

    public static final Comparator<WSBehandlingskjedeElement> NYESTE_FORST = (o1, o2) -> sammenlignBehandlingBasertPaaDato(o2, o1);

    private static int sammenlignBehandlingBasertPaaDato(WSBehandlingskjedeElement forst, WSBehandlingskjedeElement sist) {
        DateTime dato1 = forst.getInnsendtDato();
        DateTime dato2 = sist.getInnsendtDato();

        if (dato1 == null && dato2 == null) {
            return 0;
        } else if (dato1 == null) {
            return 1;
        } else if (dato2 == null) {
            return -1;
        }
        return dato1.compareTo(dato2);
    }
}
