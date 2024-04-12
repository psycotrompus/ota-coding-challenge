package ph.ota.challenge;

public class NoteException extends RuntimeException {

  NoteException(String message) {
    super(message);
  }

  NoteException(String message, Throwable cause) {
    super(message, cause);
  }
}
