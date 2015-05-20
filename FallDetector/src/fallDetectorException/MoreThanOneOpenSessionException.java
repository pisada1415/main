package fallDetectorException;

@SuppressWarnings("serial") //(non serializziamo)
public class MoreThanOneOpenSessionException extends RuntimeException{
	
	private static final String message="Tentativo di inserire nuova session con una sessione già aperta";

	public MoreThanOneOpenSessionException(){
		super(message);
	}
}
