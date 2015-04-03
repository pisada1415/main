package pisada.fallDetector;

public class InvalidSessionException extends Exception{
	private static final String message="Sessione non valida";

	public InvalidSessionException(){
		super(message); 
	}
}
