package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;

import java.util.List;

import static java.util.Arrays.asList;

public class KravdialogInformasjonHolder {

    private static List<KravdialogInformasjon> soknadsKonfigurasjoner = asList(
                new AAPOrdinaerInformasjon(),
                new AAPGjenopptakInformasjon(),
                new AAPUtlandetInformasjon(),
                new BilstonadInformasjon(),
                new TiltakspengerInformasjon(),
                new SoknadTilleggsstonader(),
                new SoknadRefusjonDagligreise()
        );

    public static KravdialogInformasjon hentKonfigurasjon(String skjemanummer) {
        for (KravdialogInformasjon soknadKonfigurasjon : soknadsKonfigurasjoner) {
            if (soknadKonfigurasjon.getSkjemanummer().contains(skjemanummer)) {
                return soknadKonfigurasjon;
            }
        }
        throw new SendSoknadException("Fant ikke config for skjemanummer: " + skjemanummer);
    }

    public static List<KravdialogInformasjon> getSoknadsKonfigurasjoner() {
        return soknadsKonfigurasjoner;
    }
}
