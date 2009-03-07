package com.billkuker.rocketry.motorsim.grain;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.validation.Validating;
import com.billkuker.rocketry.motorsim.validation.ValidationException;

public class CoredCylindricalGrain implements Grain, Validating {

	private Amount<Length> length, oD, iD;
	private boolean oInh = true, iInh = false, eInh = false;

	public CoredCylindricalGrain() {

	}

	@Override
	public Amount<Area> surfaceArea(Amount<Length> regression) {
		Amount<Length> zero = Amount.valueOf(0, SI.MILLIMETER);
		
		//Calculated regressed length
		Amount<Length> cLength = length;
		if ( !eInh ){
			cLength = cLength.minus(regression.times(2));
		}
		
		//Calculate regressed iD
		Amount<Length> cID = iD;
		if ( !iInh ){
			cID = iD.plus(regression.times(2));
		}
		
		//Calculate regressed oD
		Amount<Length> cOD = oD;
		if ( !oInh ){
			cOD = oD.minus(regression.times(2));
		}
		
		if ( cID.isGreaterThan(cOD) )
			return Amount.valueOf(0, SI.SQUARE_METRE);
		if ( cOD.isLessThan(cID) )
			return Amount.valueOf(0, SI.SQUARE_METRE);
		if ( cLength.isLessThan(zero) )
			return Amount.valueOf(0, SI.SQUARE_METRE);
		
		Amount<Area> inner = cID.times(Math.PI).times(cLength).to(SI.SQUARE_METRE);
		
		Amount<Area> outer = cOD.times(Math.PI).times(cLength).to(SI.SQUARE_METRE);
		
		Amount<Area> ends = (cOD.divide(2).pow(2).times(Math.PI)).minus(cID.divide(2).pow(2).times(Math.PI)).times(2).to(SI.SQUARE_METRE);
		
		Amount<Area> total = inner.times(iInh?0:1).plus(outer.times(oInh?0:1)).plus(ends.times(eInh?0:1));
		
		return total;
	}

	@Override
	public Amount<Volume> volume(Amount<Length> regression) {
		Amount<Length> zero = Amount.valueOf(0, SI.MILLIMETER);
		
		//Calculated regressed length
		Amount<Length> cLength = length;
		if ( !eInh ){
			cLength = cLength.minus(regression.times(2));
		}
		
		//Calculate regressed iD
		Amount<Length> cID = iD;
		if ( !iInh ){
			cID = iD.plus(regression.times(2));
		}
		
		//Calculate regressed oD
		Amount<Length> cOD = oD;
		if ( !oInh ){
			cOD = oD.minus(regression.times(2));
		}
		
		if ( cID.isGreaterThan(cOD) )
			return Amount.valueOf(0, SI.CUBIC_METRE);
		if ( cOD.isLessThan(cID) )
			return Amount.valueOf(0, SI.CUBIC_METRE);
		if ( cLength.isLessThan(zero) )
			return Amount.valueOf(0, SI.CUBIC_METRE);
	
		
		Amount<Area> end = (cOD.divide(2).pow(2).times(Math.PI)).minus(cID.divide(2).pow(2).times(Math.PI)).to(SI.SQUARE_METRE);

		return end.times(cLength).to(SI.CUBIC_METRE);
	}

	public void setLength(Amount<Length> length) {
		this.length = length;
	}

	public void setOD(Amount<Length> od) {
		oD = od;
	}

	public void setID(Amount<Length> id) {
		iD = id;
	}
	
	public void checkValidity() throws ValidationException{
		if ( iD.equals(Amount.ZERO) )
			throw new ValidationException(this, "Invalid iD");
		if ( oD.equals(Amount.ZERO) )
			throw new ValidationException(this, "Invalid oD");
		if ( length.equals(Amount.ZERO) )
			throw new ValidationException(this, "Invalid Length");
		if ( iD.isGreaterThan(oD) )
			throw new ValidationException(this, "iD > oD");
		
		if ( iInh && oInh && eInh )
			throw new ValidationException(this, "No exposed grain surface");
		
	}

	@Override
	public Amount<Length> webThickness() {
		Amount<Length> radial = null;
		if ( !iInh && !oInh )
			radial = oD.minus(iD).divide(4); //Outer and inner exposed
		else if ( !iInh || !oInh )
			radial = oD.minus(iD).divide(2); //Outer or inner exposed
		
		Amount<Length> axial = null;
		
		if ( !eInh )
			axial = length.divide(2);
		
		if ( axial == null )
			return radial;
		if ( radial == null )
			return axial;
		if ( radial.isLessThan(axial) )
			return radial;
		return axial;
	}

	public Amount<Length> getLength() {
		return length;
	}

	public Amount<Length> getOD() {
		return oD;
	}

	public Amount<Length> getID() {
		return iD;
	}
	
	
	

}
