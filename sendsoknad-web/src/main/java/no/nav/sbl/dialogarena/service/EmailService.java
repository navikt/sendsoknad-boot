package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

/**
 * Alle operasjoner som sender mail er asynkrone. Ingen bekreftelse på om eposten er sendt vil bli gitt.
 */
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired()
    @Qualifier("threadPoolTaskExecutor")
    private TaskExecutor executor;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String FRA_ADRESSE = "ikke-svar@nav.no";

    public void sendEpost(final String ePost, final String subject, final String innhold, String behandlingId) {
        final String htmlInnhold = "<p>" + innhold + "</p>";
        addTask(getMimePreperator(ePost, subject, htmlInnhold), behandlingId, 0);
    }

    private MimeMessagePreparator getMimePreperator(final String epost, final String subject, final String innhold) {
        return mimeMessage -> {
            try {
                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(epost));
                mimeMessage.setFrom(new InternetAddress(FRA_ADRESSE));
                mimeMessage.setContent(innhold, "text/html;charset=utf-8");
                mimeMessage.setSubject(subject);
            } catch (MessagingException e) {
                throw new SendSoknadException("Kunne ikke opprette e-post", e);
            }
        };
    }

    private void addTask(final MimeMessagePreparator preparator, final String behandlingId, final int loopCheck) {
        executor.execute(() -> {
            try {
                mailSender.send(preparator);
             // @TODO hva skall vi gjøre med dette ?
              //  Event event = MetricsFactory.createEvent("sendsoknad.epostsendt");
              //  event.report();
            } catch (MailException me) {
                if (loopCheck < 5) {
                    addTask(preparator, behandlingId, loopCheck + 1);
                } else {
                 //   Event event = MetricsFactory.createEvent("sendsoknad.epostsendt");
                 //   event.setFailed();
                 //   event.report();
                    logger.warn("Epost kunne ikke sendes til bruker med BrukerbehandlingId: {}", behandlingId, me);
                }
            }
        });
    }
}
