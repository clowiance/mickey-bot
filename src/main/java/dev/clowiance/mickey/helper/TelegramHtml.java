package dev.clowiance.mickey.helper;

/** Utilities for values embedded into Telegram's restricted HTML parse mode. */
public final class TelegramHtml {

    private TelegramHtml() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
