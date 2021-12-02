package com.example.ConnectMe.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordRegex {
    // used for checking password strength
    public static boolean acceptPassword(final String password){
        final String PASSWORD_PATTERN = "^(?=.*[0-9])"
                                        + "(?=.*[a-z])(?=.*[A-Z])"
                                        + "(?=.*[@#$%^&+=])"
                                        + "(?=\\S+$).{6,20}$";

        // compile it through Pattern class
        Pattern p = Pattern.compile(PASSWORD_PATTERN);
        if(password == null){
            return false;
        }
        Matcher matcher = p.matcher(password);
        return matcher.matches();
    }
}
