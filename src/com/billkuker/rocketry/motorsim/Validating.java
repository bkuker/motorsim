package com.billkuker.rocketry.motorsim;

public interface Validating {

	public class ValidationException extends Exception{
		private static final long serialVersionUID = 1L;

		public ValidationException(Validating part, String error){
			super(error);
		}
	}
	
	public void validate() throws ValidationException;
}
