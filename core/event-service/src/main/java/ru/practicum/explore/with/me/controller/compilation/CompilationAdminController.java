package ru.practicum.explore.with.me.controller.compilation;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore.with.me.interaction.api.dto.compilation.CompilationCreateDto;
import ru.practicum.explore.with.me.interaction.api.dto.compilation.CompilationRequestDto;
import ru.practicum.explore.with.me.interaction.api.dto.compilation.CompilationUpdateDto;
import ru.practicum.explore.with.me.logging.Loggable;
import ru.practicum.explore.with.me.service.compilation.CompilationService;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/admin/compilations")
public class CompilationAdminController {
    private final CompilationService compilationService;

    @PostMapping
    @Loggable
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CompilationRequestDto> create(@RequestBody @Valid CompilationCreateDto compilationCreateDto) {
        CompilationRequestDto compilationRequestDto = compilationService.create(compilationCreateDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(compilationRequestDto);
    }

    @PatchMapping("/{compId}")
    @Loggable
    public ResponseEntity<CompilationRequestDto> update(@RequestBody @Valid CompilationUpdateDto compilationUpdateDto,
                                                        @PathVariable Long compId) {
        CompilationRequestDto compilationRequestDto = compilationService.update(compilationUpdateDto, compId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(compilationRequestDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Loggable
    public void delete(@PathVariable Long compId) {
        compilationService.delete(compId);
    }
}
