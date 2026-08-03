package dev.clowiance.mickey.administration.presentation;

import dev.clowiance.mickey.administration.domain.ManagedChatSettings;
import dev.clowiance.mickey.catalog.application.UniversityCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WelcomeMessageRenderer {

    private final UniversityCatalogService catalogService;

    public String render(ManagedChatSettings settings, String memberName) {
        String community = switch (settings.audience()) {
            case APPLICANTS -> "спільноті абітурієнтів";
            case STUDENTS -> "студентській спільноті";
            case MIXED -> "спільноті УКД";
        };
        String specialty = settings.specialtyId() == null ? null : catalogService.findById(settings.specialtyId())
                .map(item -> item.code() + " " + item.name())
                .orElse(null);

        StringBuilder text = new StringBuilder("🎓 Вітаємо у ")
                .append(community).append(", <b>").append(escape(memberName)).append("</b>! 🎉\n");
        if (specialty != null) {
            text.append("\n📚 Напрям групи: <b>").append(escape(specialty)).append("</b>\n");
        }
        text.append("\n👉 /menu — актуальні спеціальності, ціни та корисна інформація")
                .append("\n👉 Дотримуйся правил чату: без спаму, небезпечних посилань і образ.")
                .append("\n\nБажаємо успіхів в УКД! 💪✨");
        return text.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "учаснику";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
