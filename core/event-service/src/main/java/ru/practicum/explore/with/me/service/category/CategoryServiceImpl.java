package ru.practicum.explore.with.me.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.explore.with.me.interaction.api.dto.category.CategoryDto;
import ru.practicum.explore.with.me.interaction.api.dto.category.NewCategoryDto;
import ru.practicum.explore.with.me.interaction.api.exception.ConflictException;
import ru.practicum.explore.with.me.interaction.api.exception.NotFoundException;
import ru.practicum.explore.with.me.mapper.CategoryMapper;
import ru.practicum.explore.with.me.model.category.Category;
import ru.practicum.explore.with.me.repository.CategoryRepository;
import ru.practicum.explore.with.me.interaction.api.util.DataProvider;
import ru.practicum.explore.with.me.interaction.api.util.ExistenceValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ExistenceValidator<Category>,
        CategoryService, DataProvider<CategoryDto, Category> {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public void validateExists(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("The required object was not found.",
                    "Category with id=" + id + " was not found");
        }
    }

    @Override
    public CategoryDto getDto(Category entity) {
        return categoryMapper.toDto(entity);
    }

    private void validateNameUnique(String categoryName) {
        if (categoryRepository.isExistName(categoryName)) {
            throw new ConflictException("The name of category should be unique.",
                    "Category with name=" + categoryName + " is already exist");
        }
    }

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto categoryDto) {
        validateNameUnique(categoryDto.getName());
        Category category = categoryRepository.save(categoryMapper.toModel(categoryDto));
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(long id) {
        Category category = findCategoryByIdOrElseThrow(id);
        if (!category.getEvents().isEmpty()) {
            throw new ConflictException("For the requested operation the conditions are not met.",
                    "The category is not empty");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(long id, NewCategoryDto categoryDto) {
        Category categoryToUpdate = findCategoryByIdOrElseThrow(id);

        if (categoryToUpdate.getName().equals(categoryDto.getName())) {
            return categoryMapper.toDto(categoryToUpdate);
        }

        validateNameUnique(categoryDto.getName());
        categoryToUpdate.setName(categoryDto.getName());
        Category category = categoryRepository.save(categoryToUpdate);
        return categoryMapper.toDto(category);
    }

    @Override
    public CategoryDto getCategory(long id) {
        Category category = findCategoryByIdOrElseThrow(id);
        return categoryMapper.toDto(category);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from, size, Sort.by("id").ascending());
        return categoryRepository.findAllDistinct(pageable).getContent()
                .stream().map(categoryMapper::toDto).toList();
    }

    private Category findCategoryByIdOrElseThrow(long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException("The required object was not found.",
                        "Category with id=" + categoryId + " was not found")
        );
    }
}