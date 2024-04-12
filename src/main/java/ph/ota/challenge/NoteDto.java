package ph.ota.challenge;

import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;

public record NoteDto(
    Integer id,

    @NotEmpty(message = "Note title cannot be empty.")
    String title,

    @NotEmpty(message = "Note body cannot be empty.")
    String body,

    LocalDateTime lastUpdated
) {}
