package hu.mik.pte.prog5.zh1vaadin.config;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class MessagesI18NProvider implements I18NProvider {

    @Override
    public List<Locale> getProvidedLocales() {
        return List.of(Locale.forLanguageTag("hu"));
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        String translation = ResourceBundle.getBundle("messages", locale).getString(key);
        return new MessageFormat(translation, locale).format(params);
    }
}
