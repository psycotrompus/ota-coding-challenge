package ph.ota.challenge;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

  @Operation(summary = "Get all notes.")
  @ApiResponses(
      @ApiResponse(responseCode = "200", description = "Notes successfully retrieved.")
  )
  @GetMapping
  public Flux<NoteDto> getNotes() {
    return noteRepo.findAll().map(note -> new NoteDto(note.getId(), note.getTitle(), note.getBody(),
        note.getLastModified()));
  }

  @Operation(summary = "Get a single note using an ID.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Note ID found.",
          content = {
              @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NoteDto.class))
          }
      ),
      @ApiResponse(responseCode = "404", description = "Note ID not found.")
  })
  @GetMapping("/{noteId}")
  public Mono<NoteDto> getNote(@PathVariable Integer noteId) {
    return noteRepo.findById(noteId)
        .map(note -> new NoteDto(note.getId(), note.getTitle(), note.getBody(), note.getLastModified()))
        .switchIfEmpty(Mono.error(new NotFoundNoteException("Note not found")));
  }

  @Operation(summary = "Create a note.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Note created."),
      @ApiResponse(responseCode = "400", description = "Note validation has failed.")
  })
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

  @Operation(summary = "Update a note.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Note updated.", content = {
          @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NoteDto.class))
      }),
      @ApiResponse(responseCode = "400", description = "Note validation has failed.")
  })
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

  @Operation(summary = "Delete a note.")
  @ApiResponses(
      @ApiResponse(responseCode = "204", description = "Note deleted.")
  )
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
