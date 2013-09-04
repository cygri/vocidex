package org.deri.vocidex;

public class VocidexException extends RuntimeException {

	public VocidexException(String msg) {
		super(msg);
	}

	public VocidexException(Exception cause) {
		super(cause);
	}

	private static final long serialVersionUID = 3767985967561221189L;
}
