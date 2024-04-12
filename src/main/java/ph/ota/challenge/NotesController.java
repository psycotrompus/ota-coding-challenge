package ph.ota.challenge;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NotesController {

  private final NoteRepository noteRepo;

  @GetMapping
  public Flux<NoteDto> getNotes() {
    return noteRepo.findAll().map(note -> new NoteDto(note.getId(), note.getTitle(), note.getBody(),
        note.getLastModified()));
  }

  @GetMapping("/{noteId}")
  public Mono<NoteDto> getNote(@PathVariable Integer noteId) {
    return noteRepo.findById(noteId)
        .map(note -> new NoteDto(note.getId(), note.getTitle(), note.getBody(), note.getLastModified()))
        .switchIfEmpty(Mono.error(new NotFoundNoteException("Note not found")));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Void> create(@RequestBody @Valid Mono<NoteDto> body) {
    return body.flatMap(note -> noteRepo.save(new NoteEntity(note.title(), note.body())))
        .onErrorResume(
            WebExchangeBindException.class::isInstance,
            ex -> Mono.error(new InvalidNoteException(((WebExchangeBindException) ex).getMessage()))
        )
        .then();
  }

  @PutMapping("/{noteId}")
  public Mono<NoteDto> update(@PathVariable Integer noteId, @RequestBody @Valid Mono<NoteDto> body) {
    return noteRepo.findById(noteId)
        .flatMap(existingNote -> body
            .doOnNext(updatedNote -> {
              existingNote.setTitle(updatedNote.title());
              existingNote.setBody(updatedNote.body());
              existingNote.setLastModified(LocalDateTime.now());
            })
            .map(updatedNote -> existingNote)
            .onErrorResume(
                WebExchangeBindException.class::isInstance,
                ex -> Mono.error(new InvalidNoteException(((WebExchangeBindException) ex).getMessage()))
            )
        )
        .flatMap(noteRepo::save)
        .map(n -> new NoteDto(n.getId(), n.getTitle(), n.getBody(), LocalDateTime.now()))
        .switchIfEmpty(Mono.error(new NotFoundNoteException("Note not found")));
  }

  @DeleteMapping("/{noteId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> delete(@PathVariable Integer noteId) {
    return noteRepo.deleteById(noteId);
  }

  @ExceptionHandler
  public ResponseEntity<Object> handleNotFound(NotFoundNoteException ex) {
    log.error("Handle not found note exception", ex);
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler
  public ResponseEntity<String> handleInvalid(InvalidNoteException ex) {
    log.error("Handle invalid note exception", ex);
    final var message = ex.getMessage();
    return ResponseEntity.badRequest().body(message);
  }

  @ExceptionHandler
  public ResponseEntity<Object> handleGeneric(Exception ex) {
    log.error("Handle generic exception", ex);
    return ResponseEntity.internalServerError().build();
  }
}
