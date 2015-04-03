package pisada.database;

public class BoolNotBoolException  extends Exception{
		private static final String message="Valori interi ammessi per 'Close': 0 , 1";
	
		public BoolNotBoolException() { super(message); }
		
		public BoolNotBoolException(String message){super(message);}

	}

