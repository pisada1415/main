package fallDetectorException;

@SuppressWarnings("serial") //(non abbiamo intenzione di serializzare)
public class DublicateNameSessionException extends Exception{
	private static final String message="Tentativo di inserire nuova sessione con nome uguale a sessione esistente";
	
	public DublicateNameSessionException() { super(message); }
	
	public DublicateNameSessionException(String message){super(message);}

}
