package com.example.obzharka.dto;

import com.example.obzharka.entity.Interest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LeadRequest(

        @NotBlank(message = "Укажите имя")
        @Size(max = 100, message = "Имя не длиннее 100 символов")
        String name,

        @NotBlank(message = "Укажите телефон")
        @Size(max = 30, message = "Слишком длинный номер")
        String phone,

        @NotNull(message = "Выберите, что вас интересует")
        Interest interest,

        @Pattern(regexp = "instagram|yandex|friends|other",
                message = "Недопустимое значение")
        String heardFrom,

        @Size(max = 2000, message = "Комментарий не длиннее 2000 символов")
        String comment,

        @Size(max = 2048, message = "Слишком длинный адрес страницы")
        String landingUrl
) {
    public LeadRequest {
        name = blankToNull(name);
        phone = blankToNull(phone);
        heardFrom = blankToNull(heardFrom);
        comment = blankToNull(comment);
        landingUrl = blankToNull(landingUrl);
    }

    private static String blankToNull(String s) {
        if (s == null) return null;
        String t = s.strip();
        return t.isEmpty() ? null : t;
    }
}
