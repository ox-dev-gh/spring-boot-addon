package ru.dpd.integration.common.email.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import ru.dpd.integration.common.email.model.Email;
import ru.dpd.integration.common.email.model.EmailFromTemplate;
import ru.dpd.integration.common.email.model.EmailType;
import ru.dpd.integration.common.email.utils.PkgUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Сервис для отправки сообщений об ошибках.
 * Первое уникальное сообщение отправляется всегда.
 * Подсчитывает повторяющиеся сообщения за заданный интервал времени,
 * отправляет одно сообщение за интервал, с указанием кол-ва повторов.
 *
 * deprecated Перестанет существовать в следующих версиях
 * @version 1.0.0
 *
 * @author Voloboev Pavel (SDS)
 */
@Slf4j
//Deprecated
public class SendMailAboutSystemErrorDefault  implements InitializingBean {

    private static final String DEFAULT_ERROR_TEMPLATE_HTML = "mail/html/about_system_error_default_error";

    private final MailAsyncSenderService mailAsyncSenderService;
    public MailAsyncSenderService getMailAsyncSenderService() {
        return this.mailAsyncSenderService;
    }

    private final SpringTemplateEngine springTemplateEngine;
    public SpringTemplateEngine getSpringTemplateEngine() {
        return this.springTemplateEngine;
    }

    private ErrorCheckTask errorCheckTask;

    private SendMailAboutSystemErrorDefaultProperty sendMailAboutSystemErrorDefaultProperty;

    /**
     * Устанавливает свойства для сервиса.
     *
     * @param sendMailAboutSystemErrorDefaultProperty свойства
     */
    public void setSendMailAboutSystemErrorDefaultProperty(
            SendMailAboutSystemErrorDefaultProperty sendMailAboutSystemErrorDefaultProperty) {
        this.sendMailAboutSystemErrorDefaultProperty = sendMailAboutSystemErrorDefaultProperty;
    }

    /**
     * Конструктор
     *
     * @param mailAsyncSenderService бин или экземпляр MailAsyncSenderService
     */
    public SendMailAboutSystemErrorDefault(MailAsyncSenderService mailAsyncSenderService) {
        log.info("SendMailAboutSystemErrorDefault - Starting...");
        this.mailAsyncSenderService = mailAsyncSenderService;
        springTemplateEngine = initSpringTemplateEngine();
    }

    /**
     * Конструктор
     *
     * @param emailSender бин или экземпляр JavaMailSender
     */
    public SendMailAboutSystemErrorDefault(JavaMailSender emailSender) {
        this(SendMailAboutSystemErrorDefault.getInstantsMailAsyncSenderService(emailSender));
    }

    private static MailAsyncSenderService getInstantsMailAsyncSenderService(JavaMailSender emailSender) {
        MailAsyncSenderService mailAsyncSenderService = new MailAsyncSenderService(emailSender);
        mailAsyncSenderService.afterPropertiesSet();
        return mailAsyncSenderService;
    }

    private SpringTemplateEngine initSpringTemplateEngine() {
        SpringTemplateEngine springTemplateEngineDefault = new SpringTemplateEngine();
        springTemplateEngineDefault.addTemplateResolver(htmlEmailTemplateResolver());
        springTemplateEngineDefault.addTemplateResolver(txtEmailTemplateResolver());
        return springTemplateEngineDefault;
    }
    private ClassLoaderTemplateResolver htmlEmailTemplateResolver() {
        final ClassLoaderTemplateResolver htmlEmailTemplateResolver = new ClassLoaderTemplateResolver();
        htmlEmailTemplateResolver.setOrder(1);
        htmlEmailTemplateResolver.setResolvablePatterns(Collections.singleton("mail/html/*"));
        htmlEmailTemplateResolver.setPrefix("/templates/");
        htmlEmailTemplateResolver.setSuffix(".html");
        htmlEmailTemplateResolver.setTemplateMode(TemplateMode.HTML);
        htmlEmailTemplateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        htmlEmailTemplateResolver.setCacheable(false);
        return htmlEmailTemplateResolver;
    }
    private ClassLoaderTemplateResolver txtEmailTemplateResolver() {
        final ClassLoaderTemplateResolver txtEmailTemplateResolver = new ClassLoaderTemplateResolver();
        txtEmailTemplateResolver.setOrder(2);
        txtEmailTemplateResolver.setResolvablePatterns(Collections.singleton("mail/text/*"));
        txtEmailTemplateResolver.setPrefix("/templates/");
        txtEmailTemplateResolver.setSuffix(".txt");
        txtEmailTemplateResolver.setTemplateMode(TemplateMode.TEXT);
        txtEmailTemplateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        txtEmailTemplateResolver.setCacheable(false);
        return txtEmailTemplateResolver;
    }

    /**
     * Обновляет интервал подсчета повторных ошибок
     */
    public void updateErrorCheckTask() {
        if (errorCheckTask != null
                && sendMailAboutSystemErrorDefaultProperty != null
                && sendMailAboutSystemErrorDefaultProperty.getReErrorCountTime() > 0) {
            errorCheckTask.setTimeInterval(sendMailAboutSystemErrorDefaultProperty.getReErrorCountTime());
        }
    }
    @Override
    public void afterPropertiesSet() {
        errorCheckTask = new ErrorCheckTask(mailAsyncSenderService);
        updateErrorCheckTask();
        Executors.newSingleThreadExecutor().execute(errorCheckTask);
        log.info("SendMailAboutSystemErrorDefault - Start completed.");
    }

    /**
     * Генерирует сообщение об ошибке и отправляет его в обработку.<br> Для формирования сообщения используется внутренний движок шаблонов и предопределенный шаблон.
     *
     * @param throwable ошибка
     */
    public void sendEmailAboutSystemError (Throwable throwable) {
        sendEmailAboutSystemError(throwable, null, null, null, false, null, null, null, null, null, null);
    }

    /**
     *  Генерирует сообщение об ошибке и отправляет его в обработку.<br> Для формирования сообщения используется переданные параметры.
     *  <br>в случае если передано null throwable нечего не делает,
     *  <br>если из остальных параметров передан null, он заменяется либо из свойств, либо используется определенный в классе.
     *
     * @param throwable                     ошибка, null - выход без действия если не указан и text
     * @param emailType                     тип сообщения (HTML, TEXT, SAMPLE), если null тогда HTML
     * @param customSpringTemplateEngine    бин или экземпляр SpringTemplateEngine, null
     * @param template                      путь и имя шаблона /templates/mail/html/<strong>"template"</strong> для html шаблона  или /templates/text/html/<strong>"template"</strong> для text шаблона
     * @param isCustomTemplate              false - по умолчанию, true - берем <strong>"template"</strong>
     * @param from                          от кого
     * @param to                            кому []
     * @param subject                       Тема сообщения
     * @param text                          Текс сообщения, null - выход без действия если не указан и throwable
     * @param properties                    Свойства шаблона {@literal Map<&lt;String, Object>&gt;}    
     * @param _level                         Уровень логирования, если null то Level.ERROR
     */
    public void sendEmailAboutSystemError (Throwable throwable,
                                           EmailType emailType,
                                           SpringTemplateEngine customSpringTemplateEngine,
                                           String template,
                                           boolean isCustomTemplate,
                                           String from,
                                           String[] to,
                                           String subject,
                                           String text,
                                           Map<String, Object> properties,
                                           System.Logger.Level _level) {

        if (throwable == null && text == null) {
            return;
        }
        System.Logger.Level level = (_level == null) ? System.Logger.Level.ERROR : _level;

        EmailFromTemplate emailFromTemplate = new EmailFromTemplate(
                customSpringTemplateEngine != null    ? customSpringTemplateEngine : springTemplateEngine,
                StringUtils.isNotBlank(from)          ? from : sendMailAboutSystemErrorDefaultProperty.getDefaultEmailFrom(),
                to !=null && to.length >0             ? to :sendMailAboutSystemErrorDefaultProperty.getDefaultTo().toArray(new String[0]),
                StringUtils.isNotBlank(subject)       ? sendMailAboutSystemErrorDefaultProperty.getDefaultSubjectPrefix().concat(subject) : sendMailAboutSystemErrorDefaultProperty.getDefaultSubject(),
                emailType != null                     ? emailType : EmailType.HTML,
                isCustomTemplate                      ? template : DEFAULT_ERROR_TEMPLATE_HTML);

        int hashCode;
        if (throwable != null) {
            emailFromTemplate.addProperty("appName", sendMailAboutSystemErrorDefaultProperty.getAppName());
            emailFromTemplate.addProperty("serverName", PkgUtils.getServerName());
            emailFromTemplate.addProperty("exceptionKey", PkgUtils.createExceptionKey(throwable));
            emailFromTemplate.addProperty("errorMsg", throwable.toString());
            List<String> strStackTrace = PkgUtils.getExceptionList(throwable);
            hashCode = strStackTrace.hashCode();
            emailFromTemplate.addProperty("stackTrace", strStackTrace);
        } else if (text != null) {
            hashCode = text.hashCode();
            emailFromTemplate.setText(text);
            if (properties != null) {
                properties.forEach(emailFromTemplate::addProperty);
            }
        } else {
            return;
        }
        errorCheckTask.sendWithNoRepeatEmail(emailFromTemplate, hashCode);
        if (throwable != null ) {
            switch (level) {
                case INFO:
                    log.info(throwable.getMessage(), throwable);
                    break;
                case WARNING:
                    log.warn(throwable.getMessage(), throwable);
                    break;
                default:
                    log.error(throwable.getMessage(), throwable);
            }

        } else {
            switch (level) {
                case INFO:
                    log.info(text);
                    break;
                case WARNING:
                    log.warn(text);
                    break;
                default:
                    log.error(text);
            }
        }
    }

    /**
     * Отправка готового сообщения
     *
     * @param EmailFromTemplate экземпляр EmailFromTemplate.
     * @param hashCode          хеш - по нему считаются повторы.
     */
    public void sendEmailAboutSystemError (EmailFromTemplate EmailFromTemplate, Integer hashCode) {
        errorCheckTask.sendWithNoRepeatEmail(EmailFromTemplate, hashCode);
    }

    /**
     * Отправляет сообщение, аналогичен методу MailAsyncSenderService.sendEmail
     *
     * @param email класс с реализованным интерфейсом Email
     */
    public void sendEmail (Email email) {
        mailAsyncSenderService.sendEmail(email);
    }

    @Data
    static class ErrorCheck {
        private int count;
        private long startTime;
        private EmailFromTemplate emailFromTemplate;

        public ErrorCheck(EmailFromTemplate emailFromTemplate) {
            this.count = 1;
            this.emailFromTemplate = emailFromTemplate;
            this.startTime = System.currentTimeMillis();
        }

        public synchronized void inc() {
            count++;
        }

        public synchronized void setStartTime(long startTime) {
            this.startTime = startTime;
        }
    }

    static class ErrorCheckTask implements Runnable {
        private static final long sleepTime = 500; //миллисекунды

        private long reErrorCountTime = 600; //секунды

        private final Map<Integer, ErrorCheck> checkErrors = new ConcurrentHashMap<>();

        public long getTimeInterval() {
            return reErrorCountTime;
        }

        public void setTimeInterval(long reErrorCountTime) {
            this.reErrorCountTime = reErrorCountTime;
        }

        private final MailAsyncSenderService mailAsyncSenderService;

        public ErrorCheckTask(MailAsyncSenderService mailAsyncSenderService) {
            this.mailAsyncSenderService = mailAsyncSenderService;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    checkErrors.forEach((key, errorCheck) -> {
                        if ((errorCheck.getStartTime() + reErrorCountTime*1000) < System.currentTimeMillis()) {
                            if (errorCheck.getCount() == 1) {
                                checkErrors.remove(key);
                            } else {
                                EmailFromTemplate emailFromTemplate = errorCheck.getEmailFromTemplate();
                                emailFromTemplate.getProperties().put("countRepeat", errorCheck.getCount());
                                emailFromTemplate.getProperties().put("timeInterval",
                                        DurationFormatUtils.formatDuration(reErrorCountTime*1000, "HH:mm:ss", true));
                                mailAsyncSenderService.sendEmail(emailFromTemplate);
                                errorCheck.setCount(1);
                                errorCheck.setStartTime(System.currentTimeMillis());
                            }
                        }
                    });
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    Thread.sleep(sleepTime);
                }
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    log.info("ErrorCheckTask - Stopping...");
                    Thread.currentThread().interrupt();
                } else {
                    log.error("ErrorCheckTask - Error " + e.getMessage(), e);
                }
            }
        }

        public synchronized void sendWithNoRepeatEmail(EmailFromTemplate emailFromTemplate, Integer hash) {
            ErrorCheck errorCheck = checkErrors.get(hash);
            if (errorCheck != null) {
                errorCheck.inc();
            } else {
                mailAsyncSenderService.sendEmail(emailFromTemplate);
                if (hash != null && hash != 0) {
                    checkErrors.put(hash, new ErrorCheck(emailFromTemplate));
                }
            }
        }
    }
}
