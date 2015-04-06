package fallDetectorException;

public class InvalidSessionException extends Exception{
	private static final String message="Sessione non valida";

	public InvalidSessionException(){
		super(message); 
	}
	
	public InvalidSessionException(String m){
		super(m); 
	}
}
