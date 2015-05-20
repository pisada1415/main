package fallDetectorException;

@SuppressWarnings("serial") //(non vogliamo serializzare)
public class InvalidFallException extends RuntimeException{
	private static final String message="Caduta non valida";

	public InvalidFallException(){
		super(message); 
	}
	
	public InvalidFallException(String message){
		super(message); 
	}
}