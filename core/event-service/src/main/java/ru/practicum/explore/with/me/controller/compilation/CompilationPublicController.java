package ru.practicum.explore.with.me.controller.compilation;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore.with.me.interaction.api.dto.compilation.CompilationRequestDto;
import ru.practicum.explore.with.me.logging.Loggable;
import ru.practicum.explore.with.me.service.compilation.CompilationService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/compilations")
public class CompilationPublicController {
    private final CompilationService compilationService;

    @GetMapping
    @Loggable
    public ResponseEntity<List<CompilationRequestDto>> get(
            @RequestParam(defaultValue = "false") Boolean pinned,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        List<CompilationRequestDto> compilations = compilationService.get(pinned, from, size);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(compilations);
    }

    @GetMapping("/{compId}")
    @Loggable
    public ResponseEntity<CompilationRequestDto> getById(@PathVariable Long compId) {
        CompilationRequestDto compilationRequestDto = compilationService.getById(compId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(compilationRequestDto);
    }
}
