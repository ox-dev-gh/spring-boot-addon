package ru.dpd.integration.common.email.utils;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Общие утилиты содержат статические методы.
 * Дублируют dpd-common, что бы развязаться с этим пакетом.
 */
public class PkgUtils {

    /**
     * Статический метод получения имени хоста, на котором запущено приложение, не зависит от OS.
     *
     * @return Получить имя хоста;
     */
    public static String getServerName() {
        String  localHost;
        try {
            localHost = InetAddress.getLocalHost().getHostName().toLowerCase();
        } catch (Exception e) {
            localHost = "unknown";
        }
        return localHost;
    }

    /**
     * Получить описание исключения в виде:
     * Наименование класса исключения: причина исключения [трассировка вызовов]
     * Пример: java.io.FileNotFoundException: test.txt (Не удается найти указанный файл) [java.io.FileInputStream.open0(Native Method), ... ru.dpd.b2b.common.util...]
     *
     * @param ex - Исключение
     * @return - текст исключения
     */
    public static List<String> getExceptionList(Throwable ex) {
        List<String> result = new ArrayList<>();
        if (ex != null) {
            result.add(ex.toString());
            result.add("------");
            result.addAll(toStringList(ex.getStackTrace()));
        }
        return result;
    }
    public static List<String> toStringList(Object[] a) {
        List<String> result = new ArrayList<>();
        if (a == null) return result;
        int iMax = a.length - 1;
        if (iMax == -1)
            return result;
        for (int i = 0; i < iMax; i++) {
            result.add(String.valueOf(a[i]));
        }
        return result;
    }
    public static String createExceptionKey(Throwable e) {

        if (e == null) return "UNKNOWN";

        String name1 = e.getClass().getSimpleName();
        Throwable e1 = null;
        Throwable ec = e.getCause();
        while (ec != null) {
            e1 = ec;
            ec = e1.getCause();
        }
        String name2;
        if (e1 == null) {
            name2 = "";
            e1 = e;
        } else {
            name2 = ": " + e1.getClass().getSimpleName();
        }
        String item3;
        try {
            if (e1 instanceof SQLException) {
                item3 = ": ORA-" + ((SQLException) e1).getErrorCode();
            } else {
                item3 = "";
            }
        } catch (Throwable tr) {
            item3 = "";
        }
        return name1 + name2 + item3;
    }

}
