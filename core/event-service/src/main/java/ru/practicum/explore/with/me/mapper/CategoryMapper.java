package ru.practicum.explore.with.me.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore.with.me.interaction.api.dto.category.CategoryDto;
import ru.practicum.explore.with.me.interaction.api.dto.category.NewCategoryDto;
import ru.practicum.explore.with.me.model.category.Category;


@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Category toModel(NewCategoryDto newCategoryDto);

    CategoryDto toDto(Category category);
}