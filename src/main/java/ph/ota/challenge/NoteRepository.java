package ph.ota.challenge;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends ReactiveCrudRepository<NoteEntity, Integer> {}
