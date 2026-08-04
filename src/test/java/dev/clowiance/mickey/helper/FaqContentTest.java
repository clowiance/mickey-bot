package dev.clowiance.mickey.helper;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class FaqContentTest {

    /** Максимальна довжина текстового повідомлення в Telegram. */
    private static final int MESSAGE_LIMIT = 4096;

    private final KeyboardHelper keyboardHelper = new KeyboardHelper();

    @Test
    void faqMenuOffersDegrees() {
        assertThat(callbacks(keyboardHelper.faqMenu())).contains("faq:degrees");
    }

    /**
     * Розділи про вступну кампанію прибрані на прохання власника бота: строки щороку
     * змінюються наказом МОН, і застарілий календар шкідливіший за його відсутність.
     */
    @Test
    void doesNotOfferAdmissionCampaignSections() {
        assertThat(callbacks(keyboardHelper.faqMenu()))
                .doesNotContain("faq:calendar", "faq:admission");
        assertThat(callbacks(keyboardHelper.mainMenu()))
                .doesNotContain("faq:calendar", "faq:admission");
    }

    @Test
    void faqTextsFitIntoSingleTelegramMessage() {
        List<String> texts = List.of(
                TextHelper.FAQ_DEGREES,
                TextHelper.FAQ_DORMS,
                TextHelper.FAQ_SPECIALTIES,
                TextHelper.FAQ_PRICES);

        assertThat(texts).allSatisfy(text ->
                assertThat(text).isNotBlank().hasSizeLessThan(MESSAGE_LIMIT));
    }

    @Test
    void degreesSectionExplainsAllThreeLevels() {
        assertThat(TextHelper.FAQ_DEGREES)
                .contains("коледж")
                .contains("Бакалавр")
                .contains("Магістр");
    }

    /**
     * Номери в різних блоках FAQ раніше були записані трьома способами
     * (<code>+380 95 755 75 79</code>, {@code +380 68 755-75-75}, {@code +38 (0342) 77-18-45}),
     * через що виглядали як помилка. Тримаємо єдиний запис {@code +380 <код> <номер>};
     * групування різне лише тому, що в мобільного абонентський номер семизначний,
     * а в міського — шестизначний.
     */
    @Test
    void writesEveryPhoneNumberInTheSameFormat() {
        List<String> faqTexts = List.of(
                TextHelper.FAQ_DORMS,
                TextHelper.FAQ_DEGREES,
                TextHelper.FAQ_SPECIALTIES,
                TextHelper.FAQ_PRICES);

        Pattern anyPhone = Pattern.compile("\\+38[0-9 ()\\-]{9,}");
        Pattern canonical = Pattern.compile("\\+380 \\d{2,3} \\d{2,3}-\\d{2}-\\d{2}");

        for (String text : faqTexts) {
            Matcher matcher = anyPhone.matcher(text);
            while (matcher.find()) {
                String phone = matcher.group().trim();
                assertThat(canonical.matcher(phone).matches())
                        .as("номер %s має бути у форматі +380 <код> <номер через дефіси>", phone)
                        .isTrue();
            }
        }
    }

    private Set<String> callbacks(InlineKeyboardMarkup keyboard) {
        return keyboard.getKeyboard().stream()
                .flatMap(List::stream)
                .map(InlineKeyboardButton::getCallbackData)
                .collect(Collectors.toSet());
    }
}
