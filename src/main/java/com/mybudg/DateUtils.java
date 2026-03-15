package com.mybudg;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.util.StringConverter;
import javafx.scene.control.DatePicker;

public class DateUtils {

    public static final String DATE_PATTERN = "dd/MM/yyyy";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    /**
     * Formats a LocalDate to DD/MM/YYYY string.
     */
    public static String format(LocalDate date) {
        if (date == null) return "";
        return DATE_FORMATTER.format(date);
    }

    /**
     * Parses a DD/MM/YYYY string to LocalDate.
     */
    public static LocalDate parse(String dateString) {
        if (dateString == null || dateString.isEmpty()) return null;
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Sets up a DatePicker to use the DD/MM/YYYY format for display and input.
     */
    public static void setupDatePicker(DatePicker datePicker) {
        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return format(date);
            }

            @Override
            public LocalDate fromString(String string) {
                return parse(string);
            }
        });
        
        // Ensure prompt text reflects the format
        datePicker.setPromptText(DATE_PATTERN.toUpperCase());
    }
}
