package ru.dpd.integration.common.email.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import ru.dpd.integration.common.email.model.Email;

import java.nio.charset.StandardCharsets;

/**
 * Отправляет сообщения, в потоке вызова.
 */
@Slf4j
public class MailSenderService {

    private final JavaMailSender emailSender;

    /**
     * Конструктор.
     *
     * @param emailSender JavaMailSender базовый интерфейс, реализован в spring-boot-starter-mail,
     *                    необходимо конфигурирование.
     */
    public MailSenderService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    /**
     * Отправляет сообщение, выбирает способ отправки, в зависимости от типа сообщения
     *
     * @param email объект с реализованным интерфейсом Email
     */
    public void sendEmail (Email email) {
        switch (email.getEmailType()) {
            case HTML:
                sendMimeMessage(email, true);
                break;
            case TEXT:
            case SIMPLE:
            default:
                sendSimpleMessage(email);
        }
    }

    /**
     * Отправляет мим-сообщение в формате html/text.
     *
     * @param email  объект с реализованным интерфейсом Email
     * @param isHtml true - html; false - text
     */
    public void sendMimeMessage (Email email, boolean isHtml) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            helper.setFrom(email.getFrom());
            helper.setTo(email.getTo());
            helper.setSubject(email.getSubject());
            helper.setText(email.getText(), isHtml);
            emailSender.send(message);
        } catch (Exception e) {
            log.error("Sending email.", e);
        }
    }

    /**
     * Отправляет простое текстовое сообщение.
     *
     * @param email объект с реализованным интерфейсом Email
     */
    public void sendSimpleMessage(Email email) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(email.getFrom());
            message.setTo(email.getTo());
            message.setSubject(email.getSubject());
            message.setText(email.getText());
            emailSender.send(message);
        } catch (Exception e) {
            log.error("Sending email.", e);
        }
    }

}
