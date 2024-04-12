package ph.ota.challenge;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Table("notes")
public class NoteEntity {

  @Id
  private Integer id;

  @NonNull
  @NotNull
  @Max(255)
  private String title;

  @NonNull
  @NotNull
  private String body;

  @LastModifiedDate
  private LocalDateTime lastModified = LocalDateTime.now();
}
