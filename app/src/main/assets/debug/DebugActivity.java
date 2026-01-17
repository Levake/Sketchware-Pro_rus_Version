package <?package_name?>;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class DebugActivity extends Activity {

    private static final Map<String, String> exceptionMap = new HashMap<String, String>() {{
        put("StringIndexOutOfBoundsException", "Недопустимая операция со строкой\n");
        put("IndexOutOfBoundsException", "Недопустимая операция со списком\n");
        put("ArithmeticException", "Недопустимая арифметическая операция\n");
        put("NumberFormatException", "Недопустимая операция с toNumber блоком\n");
        put("ActivityNotFoundException", "Недопустимая операция с intent\n");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SpannableStringBuilder formattedMessage = new SpannableStringBuilder();
        Intent intent = getIntent();
        String errorMessage = "";

        if (intent != null) {
            errorMessage = intent.getStringExtra("error");
        }

        if (!errorMessage.isEmpty()) {
            String[] split = errorMessage.split("\n");

            String exceptionType = split[0];
            String message = exceptionMap.containsKey(exceptionType) ? exceptionMap.get(exceptionType) : "";

            if (!message.isEmpty()) {
                formattedMessage.append(message);
            }

            for (int i = 1; i < split.length; i++) {
                formattedMessage.append(split[i]);
                formattedMessage.append("\n");
            }
        } else {
            formattedMessage.append("Сообщение об ошибке отсутствует.");
        }

        setTitle(getTitle() + " Зависло");

        TextView errorView = new TextView(this);
        errorView.setText(formattedMessage);
        errorView.setTextIsSelectable(true);

        HorizontalScrollView hscroll = new HorizontalScrollView(this);
        ScrollView vscroll = new ScrollView(this);

        hscroll.addView(vscroll);
        vscroll.addView(errorView);

        setContentView(hscroll);
    }
}
