package com.billkuker.rocketry.motorsim;

public class Motor extends MotorPart{
	private Chamber chamber;
	private Grain grain;
	private Nozzle nozzle;
	private Fuel fuel;
	private String name;
	
	
	public Chamber getChamber() {
		return chamber;
	}
	
	public void setChamber(Chamber chamber) {
		Chamber old = this.chamber;
		this.chamber = chamber;
		firePropertyChange("Chamber", old, chamber);
	}
	
	public Grain getGrain() {
		return grain;
	}
	
	public void setGrain(Grain grain) {
		Grain old = this.grain;
		this.grain = grain;
		firePropertyChange("Grain", old, grain);
	}
	
	public Nozzle getNozzle() {
		return nozzle;
	}
	
	public void setNozzle(Nozzle nozzle) {
		Nozzle old = this.nozzle;
		this.nozzle = nozzle;
		firePropertyChange("Nozzle", old, nozzle);
	}

	public Fuel getFuel() {
		return fuel;
	}

	public void setFuel(Fuel fuel) {
		Fuel old = this.fuel;
		this.fuel = fuel;
		firePropertyChange("Fuel", old, fuel);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		String old = this.name;
		this.name = name;
		firePropertyChange("Name", old, name);
	}
}
