package fallDetectorException;

public class AlreadyCloseSessionException extends RuntimeException {
	private static final String message="Tentativo di chiudere una sessione gia chiusa";
	
	public AlreadyCloseSessionException() { super(message); }
	
	public AlreadyCloseSessionException(String message){super(message);}
}
