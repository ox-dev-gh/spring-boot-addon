package ru.dpd.integration.common.email.service;

import lombok.extern.slf4j.Slf4j;
import ru.dpd.integration.common.email.model.Email;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class MailAsyncSenderTask implements Runnable {
    private static final long sleepTime = 500;

    private final MailSenderService mailSenderService;

    private final List<Email> emails = new CopyOnWriteArrayList<>();

    public MailAsyncSenderTask(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @Override
    public void run() {
        try {
            while (true) {
                try {
                    emails.forEach(email -> {
                        mailSenderService.sendEmail(email);
                        emails.remove(email);
                    });
                } catch (Exception e) {
                    log.error("MailAsyncSenderTask sending mail..", e);
                }
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                Thread.sleep(sleepTime);
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException ) {
                log.info("MailAsyncSender - Stopping...");
                Thread.currentThread().interrupt();
            } else {
                log.error("MailAsyncSender - Error "+e.getMessage(), e);
            }
        }
    }

    public List<Email> getEmails() {
        return emails;
    }

}
