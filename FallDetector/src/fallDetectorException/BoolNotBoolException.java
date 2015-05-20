package fallDetectorException;

@SuppressWarnings("serial") //(non abbiamo intenzione di serializzare)
public class BoolNotBoolException  extends RuntimeException{
		private static final String message="Valori interi ammessi per 'Close': 0 , 1";
	
		public BoolNotBoolException() { super(message); }
		
		public BoolNotBoolException(String message){super(message);}

	}

