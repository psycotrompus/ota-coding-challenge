package ph.ota.challenge;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = NotesController.class)
@Import(NoteRepository.class)
class NotesControllerTest {

  @MockBean
  private NoteRepository repo;

  @Autowired
  private WebTestClient client;

  void setup() {
    reset(repo);
  }

  @Test
  void getNotesShouldRespondSuccessfully() {
    // given
    var count = abs(new Random().nextInt(100));
    var notes = IntStream.range(0, count).mapToObj(idx -> Instancio.create(NoteEntity.class)).toList();
    when(repo.findAll()).thenReturn(Flux.fromIterable(notes));

    // when
    var res = client.get()
        .uri("/notes")
        .headers(headers -> {
          headers.addIfAbsent(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        })
        .exchange();

    // then
    res.expectStatus().isOk()
        .expectBodyList(NoteDto.class);
    verify(repo, times(1)).findAll();
  }

  @Test
  void getNoteShouldReturnSuccessfully() {
    // given
    var note = Instancio.create(NoteEntity.class);
    when(repo.findById(note.getId())).thenReturn(Mono.just(note));

    // when
    var res = client.get()
        .uri("/notes/{noteId}", note.getId())
        .headers(headers -> {
          headers.addIfAbsent(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        })
        .exchange();

    // then
    res.expectStatus().isOk()
        .expectBody(NoteDto.class);
    verify(repo, times(1)).findById(note.getId());
  }

  @Test
  void getNoteShouldRespondNotFound() {
    // given
    var noteId = abs(new Random().nextInt(100));
    when(repo.findById(any(Integer.class))).thenReturn(Mono.empty());

    // when
    var res = client.get()
        .uri("/notes/{noteId}", noteId)
        .headers(headers -> {
          headers.addIfAbsent(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        })
        .exchange();

    // then
    res.expectStatus().isNotFound();
    verify(repo, times(1)).findById(noteId);
  }

  @Test
  void createShouldRespondCreated() {
    // given
    when(repo.save(any())).thenAnswer(inv -> {
      var note = inv.getArgument(0, NoteEntity.class);
      note.setId(new Random().nextInt());
      return Mono.just(note);
    });
    var note = new NoteDto(null, "Sample Note Title", "Sample Note Body.", null);

    // when
    var res = client.post()
        .uri("/notes")
        .headers(headers -> {
           headers.addIfAbsent(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
         })
        .bodyValue(note)
        .exchange();

    // then
    res.expectStatus().isCreated();
    verify(repo, times(1))
        .save(argThat(n -> n.getTitle().equals(note.title()) && n.getBody().equals(note.body())));
  }

  @Test
  void createShouldRespondBadRequestOnInvalidRequest() {
    // given
    var note = new NoteDto(null, "", "", null);

    // when
    var res = client.post()
        .uri("/notes")
        .headers(headers -> headers.addIfAbsent(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .bodyValue(note)
        .exchange();

    // then
    res.expectStatus().isBadRequest();
    verify(repo, times(0)).save(any());
  }

  @Test
  void updateShouldUpdateExistingNoteSuccessfully() {
    // given
    var noteId = abs(new Random().nextInt());
    var storedNote = new NoteEntity(noteId, "Note Title", "Sample note body.", LocalDateTime.now());
    when(repo.findById(noteId)).thenReturn(Mono.just(storedNote));
    when(repo.save(any())).thenAnswer(inv -> {
      var note = inv.getArgument(0, NoteEntity.class);
      if (note.getId() == null) {
        note.setId(new Random().nextInt());
      }
      return Mono.just(note);
    });
    var request = new NoteDto(null, "New Title", "New note body.", LocalDateTime.now());

    // when
    var res = client.put()
        .uri("/notes/{noteId}", noteId)
        .headers(headers -> headers.addIfAbsent(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .bodyValue(request)
        .exchange();

    // then
    NoteDto result = res.expectStatus().isOk()
        .expectBody(NoteDto.class).returnResult().getResponseBody();
    verify(repo, times(1)).findById(noteId);
    verify(repo, times(1))
        .save(argThat(n -> result.title().equals(request.title()) && n.getBody().equals(request.body())));
  }

  @Test
  void updateShouldRespondNotFoundWhenNoteIsNotFound() {
    // given
    var noteId = abs(new Random().nextInt());
    when(repo.findById(noteId)).thenReturn(Mono.empty());

    // when
    var res = client.put()
        .uri("/notes/{noteId}", noteId)
        .headers(headers -> headers.addIfAbsent(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .bodyValue(new NoteDto(null, "New Title", "New note body.", LocalDateTime.now()))
        .exchange();

    // then
    res.expectStatus().isNotFound();
    verify(repo, times(1)).findById(noteId);
  }

  @Test
  void updateShouldRespondBadRequestWhenNoteIsInvalid() {
    // given
    var noteId = abs(new Random().nextInt());
    var storedNote = new NoteEntity(new Random().nextInt(), "Note Title", "Sample note body.",
        LocalDateTime.now());
    when(repo.findById(noteId)).thenReturn(Mono.just(storedNote));

    // when
    var res = client.put()
        .uri("/notes/{noteId}", noteId)
        .headers(headers -> headers.addIfAbsent(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .bodyValue(new NoteDto(null, "", "", LocalDateTime.now()))
        .exchange();

    // then
    res.expectStatus().isBadRequest();
    verify(repo, times(1)).findById(noteId);
  }

  @Test
  void deleteShouldExecuteSuccessfully() {
    // given
    var noteId = abs(new Random().nextInt());
    when(repo.deleteById(noteId)).thenReturn(Mono.empty());

    // when
    var res = client.delete()
        .uri("/notes/{noteId}", noteId)
        .exchange();

    // then
    res.expectStatus().isNoContent();
    verify(repo, times(1)).deleteById(noteId);
  }

}