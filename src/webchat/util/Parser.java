/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.util;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Edward
 */
public class Parser {

    private final static Logger logger = LoggerFactory.getLogger(Parser.class);
    
    public static Integer parseInt(String intStr, Integer min, Integer max, Integer defaultValue) {
        if (intStr == null) {
            return defaultValue;
        }
        try {
            int value = Integer.valueOf(intStr);
            if (min != null) {
                value = Math.max(min, value);
            }
            if (max != null) {
                value = Math.min(max, value);
            }
            return value;
        } catch (NumberFormatException e) {
            logger.debug("Can't parse " + intStr + " as int");
            return defaultValue;
        }
    }

    public static Integer parseInt(String intStr) {
        return parseInt(intStr, null, null, null);
    }

    public static Long parseDateTime(String dateStr, Long defaultValue) {
        if (dateStr == null) {
            return defaultValue;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        try {
            return sdf.parse(dateStr).getTime();
        } catch (ParseException e) {
            logger.debug("Can't parse " + dateStr + " as datetime: " + e.getMessage());
            return defaultValue;
        }
    }

    public static Long parseDateTime(String dateStr) {
        return parseDateTime(dateStr, null);
    }

    public static Field parseField(Class<?> clazz, String fieldName, String defaultFieldName) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        for (Field field : fields) {
            if (field.getName().equals(defaultFieldName)) {
                return field;
            }
        }
        throw new RuntimeException("Default field name \"" + defaultFieldName + "\" is not valid");
    }

    public static Field parseField(Class<?> clazz, String fieldName) {
        return parseField(clazz, fieldName, null);
    }

    public static String[] parseArray(String s) {
        if (s == null) {
            return new String[0];
        }
        return StringUtils.splitQuoted(s);
    }
    
    public static Boolean parseBoolean(String booleanStr, Boolean defaultValue) {
        if (booleanStr == null) {
            return defaultValue;
        }
        return Boolean.valueOf(booleanStr);
    }
    
    public static String parseString(String inStr, String defaultStr) {
        if (StringUtils.isNullOrEmpty(inStr)) {
            return defaultStr;
        }
        return inStr;
    }
}
