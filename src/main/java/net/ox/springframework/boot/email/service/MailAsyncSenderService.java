package net.ox.springframework.boot.email.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mail.javamail.JavaMailSender;
import net.ox.springframework.boot.email.model.Email;

import java.util.concurrent.Executors;

/**
 * Сервис для отправки сообщений в отдельном потоке.
 */
@Slf4j
public class MailAsyncSenderService implements InitializingBean {
    private final MailSenderService mailSenderService;
    private MailAsyncSenderTask mailAsyncSenderTask;

    /**
     * Конструктор
     *
     * @param mailSenderService MailSenderService либо бин, либо экземпляр.
     */
    public MailAsyncSenderService(MailSenderService mailSenderService) {
        log.info("MailAsyncSender - Starting...");
        this.mailSenderService = mailSenderService;
    }

    /**
     * Конструктор
     *
     * @param emailSender JavaMailSender либо бин, либо экземпляр.
     */
    public MailAsyncSenderService(JavaMailSender emailSender) {
        this(new MailSenderService(emailSender));
    }

    @Override
    public void afterPropertiesSet() {
        mailAsyncSenderTask = new MailAsyncSenderTask(mailSenderService);
        Executors.newSingleThreadExecutor().execute(mailAsyncSenderTask);
        log.info("MailAsyncSender - Start completed.");
    }

    public void sendEmail (Email email) {
        mailAsyncSenderTask.getEmails().add(email);
    }

}
