package ru.practicum.explore.with.me.interaction.api.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewCategoryDto {
    @NotBlank(message = "must not be blank")
    @Size(max = 50, message = "max size is 50")
    private String name;
}