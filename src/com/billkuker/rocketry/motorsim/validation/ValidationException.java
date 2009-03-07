package com.billkuker.rocketry.motorsim.validation;

public class ValidationException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ValidationException(Validating v, String error) {
		super(v.toString() + ": " + error);
	}
}
