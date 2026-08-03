package dev.clowiance.mickey.administration.presentation.web;

import dev.clowiance.mickey.administration.application.ChatAdministrationService;
import dev.clowiance.mickey.catalog.application.UniversityCatalogService;
import dev.clowiance.mickey.catalog.domain.StudyForm;
import dev.clowiance.mickey.administration.domain.ChatFeature;
import dev.clowiance.mickey.community.application.StudyGroupDirectory;
import dev.clowiance.mickey.community.domain.StudyGroup;
import dev.clowiance.mickey.community.domain.StudyGroupCategory;
import dev.clowiance.mickey.entity.BroadcastStatus;
import dev.clowiance.mickey.service.BroadcastService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@Validated
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "admin-panel", name = "enabled", havingValue = "true")
@RequestMapping("/admin")
public class AdminDashboardController {
    private final ChatAdministrationService chatAdministrationService;
    private final UniversityCatalogService catalogService;
    private final StudyGroupDirectory studyGroupDirectory;
    private final BroadcastService broadcastService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("chats", chatAdministrationService.findAll());
        model.addAttribute("specialties", catalogService.findAll());
        model.addAttribute("groups", studyGroupDirectory.findAll());
        model.addAttribute("pendingBroadcasts", broadcastService.count(BroadcastStatus.PENDING));
        model.addAttribute("failedBroadcasts", broadcastService.count(BroadcastStatus.DEAD_LETTER));
        model.addAttribute("broadcasts", broadcastService.recent());
        return "admin/dashboard";
    }

    @PostMapping("/groups")
    public String addGroup(@RequestParam @NotBlank String specialty,
                           @RequestParam(required = false) String course,
                           @RequestParam @Pattern(regexp = "https://t\\.me/(\\+|joinchat/)[A-Za-z0-9_-]+") String inviteLink,
                           @RequestParam(required = false) String note) {
        studyGroupDirectory.save(new StudyGroup(
                StudyGroupCategory.UNIVERSITY, specialty.trim(), nullable(course), inviteLink.trim(), nullable(note)));
        return "redirect:/admin#groups";
    }

    @PostMapping("/groups/{id}/toggle")
    public String toggleGroup(@PathVariable Long id) {
        StudyGroup group = studyGroupDirectory.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Групу не знайдено"));
        studyGroupDirectory.setActive(id, !group.active());
        return "redirect:/admin#groups";
    }

    @PostMapping("/catalog/{id}/toggle")
    public String toggleSpecialty(@PathVariable Long id) {
        catalogService.toggleActive(id);
        return "redirect:/admin#catalog";
    }

    @PostMapping("/catalog/{id}/tuition")
    public String updateTuition(@PathVariable Long id,
                                @RequestParam(required = false) Long fullTime,
                                @RequestParam(required = false) Long partTime) {
        if (fullTime != null) {
            catalogService.updateTuition(id, StudyForm.FULL_TIME, fullTime);
        }
        if (partTime != null) {
            catalogService.updateTuition(id, StudyForm.PART_TIME, partTime);
        }
        return "redirect:/admin#catalog";
    }

    @PostMapping("/chats/{chatId}/toggle/{feature}")
    public String toggleChatFeature(@PathVariable Long chatId, @PathVariable ChatFeature feature) {
        chatAdministrationService.toggle(chatId, feature);
        return "redirect:/admin#chats";
    }

    @PostMapping("/broadcasts/{id}/cancel")
    public String cancelBroadcast(@PathVariable Long id) {
        broadcastService.cancel(id);
        return "redirect:/admin#broadcasts";
    }

    @PostMapping("/broadcasts/{id}/retry")
    public String retryBroadcast(@PathVariable Long id) {
        broadcastService.retry(id);
        return "redirect:/admin#broadcasts";
    }

    private String nullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
