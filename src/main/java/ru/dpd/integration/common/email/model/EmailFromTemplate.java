package ru.dpd.integration.common.email.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * Реализация интерфейса Email для сообщения сгенерированных по шаблону SpringTemplateEngine (thymeleaf).
 */
@Data
public class EmailFromTemplate implements Email {

    private String from;
    private String[] to;
    private String subject;
    private String text;
    private EmailType emailType;

    /**
     * имя шаблона
     */
    private String template;

    /**
     * Свойства сообщения
     */
    private final Map<String, Object> properties;

    /**
     * SpringTemplateEngine - бин из контекста приложения или новый экземпляр.
     */
    private final SpringTemplateEngine templateEngine;

    /**
     * Экземпляр нового объекта Email из шаблона.
     */
    public EmailFromTemplate() {
        this.templateEngine = null;
        this.properties = new HashMap<>();
    }

    /**
     * Экземпляр нового объекта Email из шаблона.
     *
     * @param templateEngine SpringTemplateEngine
     */
    public EmailFromTemplate (SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.properties = new HashMap<>();
    }

    /**
     * Экземпляр нового объекта Email из шаблона.
     *
     * @param templateEngine SpringTemplateEngine
     * @param from           отправитель
     * @param to             Массив получателей
     * @param subject        Тема
     * @param emailType      Тип
     * @param template       Название шаблона
     */
    public EmailFromTemplate (SpringTemplateEngine templateEngine,
                              String from,
                              String[] to,
                              String subject,
                              EmailType emailType,
                              String template) {
        this.to = to;
        this.from = from;
        this.subject = subject;
        this.template = template;
        this.emailType = emailType;
        this.properties = new HashMap<>();
        this.templateEngine = templateEngine;
    }

    /**
     * Добавить свойство
     *
     * @param name Тег
     * @param obj  Объект
     */
    public void addProperty (String name, Object obj) {
        this.properties.put(name, obj);
    }

    /**
     * Удалить свойство
     *
     * @param name Тег
     */
    public void remProperty (String name) {
        this.properties.remove(name);
    }

    /**
     * Очистить все свойства
     */
    public void clearProperty () {
        this.properties.clear();
    }

    /**
     * Получить текст сообщения, если задан templateEngine и шаблон, то генерируем по шаблоны и свойствам,
     * если нет то из переменной text.
     *
     * @return текст сообщения
     */
    @Override
    public String getText() {
        if (templateEngine != null && StringUtils.isNotBlank(template)) {
            Context context = new Context();
            context.setVariables(properties);
            return templateEngine.process(template, context);
        } else {
            return text;
        }
    }

}
