package fallDetectorException;

public class DublicateNameSessionException extends Exception{
	private static final String message="Tentativo di inserire nuova sessione con nome uguale a sessione esistente";
	
	public DublicateNameSessionException() { super(message); }
	
	public DublicateNameSessionException(String message){super(message);}

}
