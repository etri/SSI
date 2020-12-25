package com.iitp.util;

import android.text.TextUtils;

/**
 * String utility
 */
public class StringUtils{
    /**
     * check contained special character
     * @param string string to check
     * @return if not contained special character, return false otherwise true
     */
    public static boolean hasSpecialCharacter(String string) {
        if (TextUtils.isEmpty(string)) {
            return false;
        }

        for (int i = 0; i < string.length(); i++) {
            if (!Character.isLetterOrDigit(string.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    /**
     * string equals
     * @param s1 source 1
     * @param s2 source 2
     * @return equals
     */
    public static boolean equals(String s1, String s2) {
        if (s1 != null && s2 != null && s1.equals(s2)) {
            return true;
        }
        else if (s1 == null && s2 == null) {
            return true;
        }
        return false;
    }

    /**
     * string equals ignore case
     * @param s1 source 1
     * @param s2 source 2
     * @return equals
     */
    public static boolean equalsIgnoreCase(String s1, String s2) {
        if (s1 != null && s2 != null && s1.equalsIgnoreCase(s2)) {
            return true;
        }
        else if ((s1 == null || s1.length() == 0) && (s2 == null || s2.length() == 0)) {
            return true;
        }
        return false;
    }

    public static boolean empty(String s) {
        return s == null || s.trim().length() == 0;
    }
}
