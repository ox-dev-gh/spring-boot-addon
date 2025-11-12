package ru.dpd.integration.common.email.model;

/**
 * Интерфейс Email - описывает сообщение для отправки.
 */
public interface Email {

    /**
     * Получить массив адресов, получателей сообщения.
     *
     * @return массив адресов [ ]
     */
    String[] getTo();

    /**
     * Получить адрес отправителя.
     *
     * @return адрес отправителя
     */
    String getFrom();

    /**
     * Получить тему сообщения
     *
     * @return тема сообщения
     */
    String getSubject();

    /**
     * Получить текст сообщения
     *
     * @return текст сообщения
     */
    String getText();

    /**
     * Получить тип сообщения (HTML, TEXT, SIMPLE)
     *
     * @return тип сообщения (HTML, TEXT, SIMPLE)
     */
    EmailType getEmailType();

}
