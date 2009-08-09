package com.billkuker.rocketry.motorsim;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import com.billkuker.rocketry.motorsim.Validating.ValidationException;

public class Motor implements Validating{
	private Chamber chamber;
	private Grain grain;
	private Nozzle nozzle;
	private Fuel fuel;
	private String name;
	
	public void validate() throws ValidationException {
		if ( chamber.chamberVolume().isLessThan(grain.volume(Amount.valueOf(0, SI.MILLIMETER)))){
			throw new ValidationException(this, "Fuel does not fit in chamber");
		}
		if ( chamber instanceof Validating )
			((Validating)chamber).validate();
		if ( grain instanceof Validating )
			((Validating)grain).validate();
		if ( nozzle instanceof Validating )
			((Validating)nozzle).validate();
		if ( fuel instanceof Validating )
			((Validating)fuel).validate();
	}
	
	public Chamber getChamber() {
		return chamber;
	}
	
	public void setChamber(Chamber chamber) {
		this.chamber = chamber;
	}
	
	public Grain getGrain() {
		return grain;
	}
	
	public void setGrain(Grain grain) {
		this.grain = grain;
	}
	
	public Nozzle getNozzle() {
		return nozzle;
	}
	
	public void setNozzle(Nozzle nozzle) {
		this.nozzle = nozzle;
	}

	public Fuel getFuel() {
		return fuel;
	}

	public void setFuel(Fuel fuel) {
		this.fuel = fuel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
