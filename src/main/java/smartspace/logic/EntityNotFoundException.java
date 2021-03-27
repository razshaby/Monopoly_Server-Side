package smartspace.logic;

public class EntityNotFoundException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7967789026511217452L;

	public EntityNotFoundException() {
		super();
	}

	public EntityNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public EntityNotFoundException(String message) {
		super(message);
	}

	public EntityNotFoundException(Throwable cause) {
		super(cause);
	}
	

}
