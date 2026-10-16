package com.example.obzharka.util;

import com.example.obzharka.exception.InvalidPhoneException;
import java.util.regex.Pattern;
/**
 * Приводит белорусский мобильный номер к виду +375XXXXXXXXX.
 * Допущение: городские номера не поддерживаются, только коды 25, 29, 33, 44.
 */
public final class PhoneNormalizer {

    public static final String ERROR_MESSAGE =
            "Введите белорусский номер, например +375 29 123-45-67";

    private static final Pattern MOBILE = Pattern.compile("^375(25|29|33|44)[0-9]{7}$");

    private PhoneNormalizer() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            throw new InvalidPhoneException(ERROR_MESSAGE);
        }

        String digits = raw.replaceAll("[^0-9]", "");

        if (digits.length() == 11 && digits.startsWith("80")) {
            digits = "375" + digits.substring(2);   // 8 029 123-45-67
        } else if (digits.length() == 10 && digits.startsWith("0")) {
            digits = "375" + digits.substring(1);   // 029 123-45-67
        }

        if (!MOBILE.matcher(digits).matches()) {
            throw new InvalidPhoneException(ERROR_MESSAGE);
        }
        return "+" + digits;
    }
}