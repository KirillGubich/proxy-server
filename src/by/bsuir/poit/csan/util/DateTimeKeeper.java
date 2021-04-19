package by.bsuir.poit.csan.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeKeeper {
    private static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";

    private DateTimeKeeper() {
    }

    public static String getCurrentDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        return LocalDateTime.now().format(formatter);
    }
}
