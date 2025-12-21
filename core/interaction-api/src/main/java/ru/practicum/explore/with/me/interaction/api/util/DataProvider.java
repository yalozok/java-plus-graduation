package ru.practicum.explore.with.me.interaction.api.util;

public interface DataProvider<D, E> {
    D getDto(E entity);
}
