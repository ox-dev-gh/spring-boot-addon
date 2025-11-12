package net.ox.springframework.boot.email.service;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import net.ox.springframework.boot.email.utils.EnvDeployment;

import java.util.List;

/**
 * Параметры для сервиса для отправки сообщений об ошибках
 */
@Data
public class SendMailAboutSystemErrorDefaultProperty {
    private long reErrorCountTime = 600;
    private String appName = "unknown";
    private List<String> defaultTo;

    private final EnvDeployment envDeployment = EnvDeployment.getEnvApp();

    public String getDefaultEmailPostfix() {
        return EnvDeployment.PROD.equals(envDeployment) ? "" : "-" + envDeployment.name().toLowerCase();
    }

    public String getDefaultSubjectPrefix() {
        return EnvDeployment.PROD.equals(envDeployment) ? "" : "(" + envDeployment.name() + ") ";
    }

    public String getDefaultEmailFrom() {

        return String.format("%1$s%2$s <%1$s%2$s@dpd.ru>", appName, getDefaultEmailPostfix());
    }
    public String getDefaultSubject () {
        return String.format("%1$sОшибка в приложении: %2$s", getDefaultSubjectPrefix(), (StringUtils.isNotBlank(appName) ? appName : "unknown"));
    }

    /**
     * Возвращает новый SendMailAboutSystemErrorDefault с текущими свойствами.
     *
     * @param emailSender экземпляр JavaMailSender
     * @return новый экземпляр SendMailAboutSystemErrorDefault
     */
    public SendMailAboutSystemErrorDefault create(JavaMailSender emailSender) {
        SendMailAboutSystemErrorDefault sendMailAboutSystemErrorDefault = new SendMailAboutSystemErrorDefault(emailSender);
        sendMailAboutSystemErrorDefault.setSendMailAboutSystemErrorDefaultProperty(this);
        return sendMailAboutSystemErrorDefault;
    }

    /**
     * Возвращает новый SendMailAboutSystemErrorDefault с текущими свойствами.
     *
     * @param mailAsyncSenderService экземпляр MailAsyncSenderService
     * @return  новый экземпляр SendMailAboutSystemErrorDefault
     */
    public SendMailAboutSystemErrorDefault create(MailAsyncSenderService mailAsyncSenderService) {
        SendMailAboutSystemErrorDefault sendMailAboutSystemErrorDefault = new SendMailAboutSystemErrorDefault(mailAsyncSenderService);
        sendMailAboutSystemErrorDefault.setSendMailAboutSystemErrorDefaultProperty(this);
        return sendMailAboutSystemErrorDefault;
    }

}
