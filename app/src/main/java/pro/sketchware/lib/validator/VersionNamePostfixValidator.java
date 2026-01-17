package pro.sketchware.lib.validator;

import android.content.Context;

import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

import a.a.a.MB;

public class VersionNamePostfixValidator extends MB {

    private static final Pattern VERSION_NAME_POSTFIX_PATTERN = Pattern.compile("^[a-zA-Z0-9_]*");

    public VersionNamePostfixValidator(Context context, TextInputLayout textInputLayout) {
        super(context, textInputLayout);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String se = s.toString();
        if (VERSION_NAME_POSTFIX_PATTERN.matcher(se).matches()) {
            b.setError(null);
            d = true;
        } else if (se.contains(" ")) {
            b.setError("Пробелы недопустимы для предотвращения сбоев");
            d = false;
        } else {
            b.setError("Используйте только буквы (a-zA-Z), цифры и специальные символы (_).");
            d = false;
        }
    }
}
