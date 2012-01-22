package com.billkuker.rocketry.motorsim;

import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

public class Motor implements Validating{
	private Chamber chamber;
	private Grain grain;
	private Nozzle nozzle;
	private Fuel fuel;
	private String name;
	private Amount<Duration> ejectionDelay = Amount.valueOf(5, SI.SECOND);
	
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

	public Amount<Duration> getEjectionDelay() {
		return ejectionDelay;
	}

	public void setEjectionDelay(Amount<Duration> ejectionDelay) {
		this.ejectionDelay = ejectionDelay;
	}
}
