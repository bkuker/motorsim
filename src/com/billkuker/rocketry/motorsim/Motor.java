package com.billkuker.rocketry.motorsim;

public class Motor {
	private Chamber chamber;
	private Grain grain;
	private Nozzle nozzle;
	private Fuel fuel;
	
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
}
