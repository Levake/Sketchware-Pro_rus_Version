package kellinwood.security.zipsigner;

/**
 * Default resource adapter.
 */
public class DefaultResourceAdapter implements ResourceAdapter {

    @Override
    public String getString(Item item, Object... args) {

        return switch (item) {
            case INPUT_SAME_AS_OUTPUT_ERROR ->
                    "Входные и выходные файлы одинаковы. Укажите другое имя для выходных данных.";
            case AUTO_KEY_SELECTION_ERROR -> "Не удается автоматически выбрать ключ для подписи " + args[0];
            case LOADING_CERTIFICATE_AND_KEY -> "Загрузка сертификата и закрытого ключа";
            case PARSING_CENTRAL_DIRECTORY -> "Разбор центрального каталога входных данных";
            case GENERATING_MANIFEST -> "Создание манифеста";
            case GENERATING_SIGNATURE_FILE -> "Создание файла подписи";
            case GENERATING_SIGNATURE_BLOCK -> "Создание файла блока подписи";
            case COPYING_ZIP_ENTRY -> String.format("Копирование zip индекса %d в %d", args[0], args[1]);
            default -> throw new IllegalArgumentException("Неизвестный элемент " + item);
        };

    }
}
