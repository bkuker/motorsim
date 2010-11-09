package com.billkuker.rocketry.motorsim.fuel;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;


//TODO Implement MotorPart
public class EditableFuel extends SaintRobertFuel {
	private String name;
	private double a = 0.0665;
	private double n = 0.319;
	private double combustionEfficiency = .97;
	private double densityRatio = .96;
	private Amount<VolumetricDensity> idealDensity = Amount.valueOf(1889, 0, SI.KILOGRAM.divide(SI.METER.pow(3))).to(VolumetricDensity.UNIT);
	CombustionProduct combustionProduct = new EditableCombustionProduct();
	private SaintRobertFuel.Type type = SaintRobertFuel.Type.NONSI;
	
	public EditableFuel() {
		super(SaintRobertFuel.Type.NONSI);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	@Override
	protected double burnrateCoefficient(Amount<Pressure> pressure) {
		return a;
	}

	@Override
	protected double burnrateExponent(Amount<Pressure> pressure) {
		return n;
	}


	public double getA() {
		return a;
	}

	public void setA(double a) {
		this.a = a;
	}

	public double getCombustionEfficiency() {
		return combustionEfficiency;
	}

	public void setCombustionEfficiency(double combustionEfficiency) {
		this.combustionEfficiency = combustionEfficiency;
	}

	public double getDensityRatio() {
		return densityRatio;
	}

	public void setDensityRatio(double densityRatio) {
		this.densityRatio = densityRatio;
	}

	public Amount<VolumetricDensity> getIdealDensity() {
		return idealDensity;
	}

	public void setIdealDensity(Amount<VolumetricDensity> idealDensity) {
		this.idealDensity = idealDensity;
	}

	public double getN() {
		return n;
	}

	public void setN(double n) {
		this.n = n;
	}

	public CombustionProduct getCombustionProduct() {
		return combustionProduct;
	}

	public void setCombustionProduct(CombustionProduct combustionProduct) {
		this.combustionProduct = combustionProduct;
	}

	public SaintRobertFuel.Type getType() {
		return type;
	}

	public void setType(SaintRobertFuel.Type type) {
		this.type = type;
	}


}
