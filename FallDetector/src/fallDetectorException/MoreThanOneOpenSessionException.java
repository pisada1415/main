package fallDetectorException;

public class MoreThanOneOpenSessionException extends Exception{
	
	private static final String message="Tentativo di inserire nuova session con una sessione già aperta";

	public MoreThanOneOpenSessionException(){
		super(message);
	}
}
