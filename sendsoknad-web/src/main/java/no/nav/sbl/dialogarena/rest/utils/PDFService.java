package no.nav.sbl.dialogarena.rest.utils;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.service.HtmlGenerator;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import static no.nav.sbl.dialogarena.utils.PDFFabrikk.lagPdfFil;

@Component
public class PDFService {

    @Autowired
    private HtmlGenerator pdfTemplate;

    @Autowired
    private VedleggService vedleggService;

    @Autowired
    private WebSoknadConfig webSoknadConfig;


    public byte[] genererKvitteringPdf(WebSoknad soknad, String servletPath) {
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());
        String skjemanummer = soknad.getskjemaNummer();
        KravdialogInformasjon kravdialogInformasjon = KravdialogInformasjonHolder.hentKonfigurasjon(skjemanummer);
        return lagPdfFraSkjema(soknad, kravdialogInformasjon.getKvitteringTemplate(), servletPath);
    }

    public byte[] genererEttersendingPdf(WebSoknad soknad, String servletPath) {
        return lagPdfFraSkjema(soknad, "skjema/ettersending/dummy", servletPath);
    }

    public byte[] genererOppsummeringPdf(WebSoknad soknad, String servletPath, boolean fullSoknad) {
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());

        if (webSoknadConfig.brukerNyOppsummering(soknad.getSoknadId())) {
            return lagPdf(soknad, servletPath, fullSoknad);
        } else {
            return lagPdfFraSkjema(soknad, "/skjema/" + soknad.getSoknadPrefix(), servletPath);
        }
    }


    private byte[] lagPdf(WebSoknad soknad, String servletPath, boolean fullSoknad) {
        String pdfMarkup;
        try {
            pdfMarkup = pdfTemplate.fyllHtmlMalMedInnhold(soknad, fullSoknad);
        } catch (IOException e) {
            throw new SendSoknadException("Kunne ikke lage markup for skjema", e);
        }
        return lagPdfFraMarkup(pdfMarkup, servletPath);
    }

    private byte[] lagPdfFraSkjema(WebSoknad soknad, String hbsSkjemaPath, String servletPath) {
        String pdfMarkup;
        try {
            pdfMarkup = pdfTemplate.fyllHtmlMalMedInnhold(soknad, hbsSkjemaPath);
        } catch (IOException e) {
            throw new SendSoknadException("Kunne ikke lage markup for skjema " + hbsSkjemaPath, e);
        }
        return lagPdfFraMarkup(pdfMarkup, servletPath);
    }

    private byte[] lagPdfFraMarkup(String pdfMarkup, String servletPath) {
        return lagPdfFil(pdfMarkup, new File(servletPath).toURI().toString());
    }
}
