package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeption.DirectorNotFound;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorsController {

    private final DirectorService directorService;

    @PostMapping
    public Director addDirector(@Valid @RequestBody Director director) throws DirectorNotFound {
        return directorService.addDirector(director);
    }

    @GetMapping
    public List<Director> getAllDirectors() {
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable int id) throws DirectorNotFound {
        return directorService.getDirectorById(id);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) throws DirectorNotFound {
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void deleteDirectorById(@PathVariable int id) throws DirectorNotFound {
        directorService.deleteDirectorById(id);
    }

}