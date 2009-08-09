package com.billkuker.rocketry.motorsim;

public interface Validating {

	public class ValidationException extends Exception{
		public ValidationException(Validating part, String error){
			super(error);
		}
	}
	
	public void validate() throws ValidationException;
}
