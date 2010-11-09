package com.billkuker.rocketry.motorsim.fuel;

import javax.measure.quantity.Temperature;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.RocketScience.MolarWeight;

public class EditableCombustionProduct implements Fuel.CombustionProduct{
	private Amount<MolarWeight> effectiveMolarWeight = Amount.valueOf("41.98 kg/kmol").to(MolarWeight.UNIT);;
	private Amount<Temperature> idealCombustionTemperature = Amount.valueOf(1720, SI.KELVIN);;
	private double ratioOfSpecificHeats = 1.133;
	private double ratioOfSpecificHeats2Phase = 1.044;
	public Amount<MolarWeight> getEffectiveMolarWeight() {
		return effectiveMolarWeight;
	}
	public void setEffectiveMolarWeight(Amount<MolarWeight> effectiveMolarWeight) {
		this.effectiveMolarWeight = effectiveMolarWeight;
	}
	public Amount<Temperature> getIdealCombustionTemperature() {
		return idealCombustionTemperature;
	}
	public void setIdealCombustionTemperature(
			Amount<Temperature> idealCombustionTemperature) {
		this.idealCombustionTemperature = idealCombustionTemperature;
	}
	public double getRatioOfSpecificHeats() {
		return ratioOfSpecificHeats;
	}
	public void setRatioOfSpecificHeats(double ratioOfSpecificHeats) {
		this.ratioOfSpecificHeats = ratioOfSpecificHeats;
	}
	public double getRatioOfSpecificHeats2Phase() {
		return ratioOfSpecificHeats2Phase;
	}
	public void setRatioOfSpecificHeats2Phase(double ratioOfSpecificHeats2Phase) {
		this.ratioOfSpecificHeats2Phase = ratioOfSpecificHeats2Phase;
	}
}