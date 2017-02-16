package com.dmiesoft.fitpomodoro.utils;

import android.text.InputFilter;
import android.text.Spanned;

public class EditTextInputFilter implements InputFilter {

    private String blockedCharacters;

    public EditTextInputFilter(String blockedCharacters){
        this.blockedCharacters = blockedCharacters;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        if (source != null && blockedCharacters.contains((String.valueOf(source)))) {
            return "";
        }
        return null;
    }
}
