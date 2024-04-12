package ph.ota.challenge;

public class InvalidNoteException extends NoteException {

  InvalidNoteException(String message) {
    super(message);
  }

  InvalidNoteException(String message, Throwable cause) {
    super(message, cause);
  }
}
