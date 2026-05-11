package com.classroom.app.service;

import java.util.regex.Pattern;

public final class PhoneValidator {
    private static final Pattern E164_LIKE = Pattern.compile("^\\+?[1-9]\\d{7,14}$");

    private PhoneValidator() {}

    public static boolean isValid(String phone) {
        return phone != null && E164_LIKE.matcher(phone.trim()).matches();
    }
}
