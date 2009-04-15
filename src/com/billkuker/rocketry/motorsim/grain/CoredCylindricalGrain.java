package com.billkuker.rocketry.motorsim.grain;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.MotorPart;
import com.billkuker.rocketry.motorsim.visual.Editor;
import com.billkuker.rocketry.motorsim.visual.GrainPanel;


public class CoredCylindricalGrain extends ExtrudedGrain implements MotorPart.Validating {

	private Amount<Length> oD, iD;
	private boolean outerSurfaceInhibited = true, innerSurfaceInhibited = false;

	public CoredCylindricalGrain() {

		oD = Amount.valueOf(30, SI.MILLIMETER);
		iD = Amount.valueOf(10, SI.MILLIMETER);
	}
	
	@Deprecated
	public void inhibit(boolean in, boolean out, boolean end){
		outerSurfaceInhibited = out;
		innerSurfaceInhibited = in;
	}

	@Override
	public Amount<Area> surfaceArea(Amount<Length> regression) {
		Amount<Length> zero = Amount.valueOf(0, SI.MILLIMETER);
		if ( regression.isLessThan(zero) )
			return Amount.valueOf(0, SI.SQUARE_METRE);
		
		//Calculated regressed length
		Amount<Length> cLength = regressedLength(regression);
		
		//Calculate regressed iD
		Amount<Length> cID = iD;
		if ( !innerSurfaceInhibited ){
			cID = iD.plus(regression.times(2));
		}
		
		//Calculate regressed oD
		Amount<Length> cOD = oD;
		if ( !outerSurfaceInhibited ){
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
		
		Amount<Area> end = (cOD.divide(2).pow(2).times(Math.PI)).minus(cID.divide(2).pow(2).times(Math.PI)).to(SI.SQUARE_METRE);
		
		Amount<Area> total = inner.times(innerSurfaceInhibited?0:1).plus(outer.times(outerSurfaceInhibited?0:1)).plus(end.times(numberOfBurningEnds(regression)));
		
		return total;
	}

	@Override
	public Amount<Volume> volume(Amount<Length> regression) {
		Amount<Length> zero = Amount.valueOf(0, SI.MILLIMETER);
		
		if ( regression.isLessThan(zero) )
			regression = zero;
		
		//Calculated regressed length
		Amount<Length> cLength = regressedLength(regression);
		
		//Calculate regressed iD
		Amount<Length> cID = iD;
		if ( !innerSurfaceInhibited ){
			cID = iD.plus(regression.times(2));
		}
		
		//Calculate regressed oD
		Amount<Length> cOD = oD;
		if ( !outerSurfaceInhibited ){
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

	public void setOD(Amount<Length> od) throws PropertyVetoException {
		fireVetoableChange("od", this.oD, od);
		Amount<Length> old = this.oD;
		this.oD = od;
		firePropertyChange("OD", old, oD);
	}

	public void setID(Amount<Length> id) throws PropertyVetoException {
		fireVetoableChange("id", this.iD, id);
		Amount<Length> old = this.iD;
		iD = id;
		firePropertyChange("ID", old, iD);
	}
	
	public void checkValidity() throws ValidationException{
		if ( iD.equals(Amount.ZERO) )
			throw new ValidationException(this, "Invalid iD");
		if ( oD.equals(Amount.ZERO) )
			throw new ValidationException(this, "Invalid oD");
		if ( getLength().equals(Amount.ZERO) )
			throw new ValidationException(this, "Invalid Length");
		if ( iD.isGreaterThan(oD) )
			throw new ValidationException(this, "iD > oD");
		
		if ( innerSurfaceInhibited && outerSurfaceInhibited )
			throw new ValidationException(this, "No exposed grain surface");
		
	}

	@Override
	public Amount<Length> webThickness() {
		if ( innerSurfaceInhibited && outerSurfaceInhibited ){
			return oD; //TODO gotta move this to the end
		}
		
		Amount<Length> radial = null;
		if ( !innerSurfaceInhibited && !outerSurfaceInhibited )
			radial = oD.minus(iD).divide(4); //Outer and inner exposed
		else if ( !innerSurfaceInhibited || !outerSurfaceInhibited )
			radial = oD.minus(iD).divide(2); //Outer or inner exposed
		
		Amount<Length> axial = null;
		
		if ( numberOfBurningEnds(Amount.valueOf(0, SI.MILLIMETER)) != 0 )
			axial = getLength().divide(numberOfBurningEnds(Amount.valueOf(0, SI.MILLIMETER)));
		
		if ( axial == null )
			return radial;
		if ( radial == null )
			return axial;
		if ( radial.isLessThan(axial) )
			return radial;
		return axial;
	}

	public Amount<Length> getOD() {
		return oD;
	}

	public Amount<Length> getID() {
		return iD;
	}
	
	@Override
	public java.awt.geom.Area getCrossSection(Amount<Length> regression){
		Amount<Length> zero = Amount.valueOf(0, SI.MILLIMETER);
		if ( regression.isLessThan(zero) )
			regression = zero;
		double rmm = regression.doubleValue(SI.MILLIMETER);
		double oDmm = oD.doubleValue(SI.MILLIMETER);
		double iDmm = iD.doubleValue(SI.MILLIMETER);

		if ( !outerSurfaceInhibited )
			oDmm -= 2.0 * rmm;
		if ( !innerSurfaceInhibited )
			iDmm += 2.0 * rmm;
		
		Shape oDs = new Ellipse2D.Double(-oDmm/2.0, -oDmm/2.0, oDmm, oDmm);
		Shape iDs = new Ellipse2D.Double(-iDmm/2.0, -iDmm/2.0, iDmm, iDmm);
		
		java.awt.geom.Area a = new java.awt.geom.Area(oDs);
		a.subtract(new java.awt.geom.Area(iDs));
		return a;
	}
	
	public java.awt.geom.Area getSideView(Amount<Length> regression){
		Amount<Length> zero = Amount.valueOf(0, SI.MILLIMETER);
		if ( regression.isLessThan(zero) )
			regression = zero;
		double rmm = regression.doubleValue(SI.MILLIMETER);
		double oDmm = oD.doubleValue(SI.MILLIMETER);
		double iDmm = iD.doubleValue(SI.MILLIMETER);
		double lmm = regressedLength(regression).doubleValue(SI.MILLIMETER);
		double length = getLength().doubleValue(SI.MILLIMETER);

		if ( !outerSurfaceInhibited )
			oDmm -= 2.0 * rmm;
		if ( !innerSurfaceInhibited )
			iDmm += 2.0 * rmm;
		
		java.awt.geom.Area a = new java.awt.geom.Area();
		
		double top = -lmm/2;
		if ( isForeEndInhibited() && !isAftEndInhibited() )
			top = -length/2;
		else if ( isAftEndInhibited() && !isForeEndInhibited() )
			top = length/2-lmm;
		
		a.add( new java.awt.geom.Area(new Rectangle2D.Double(-oDmm/2,top,oDmm, lmm)));
		a.subtract( new java.awt.geom.Area(new Rectangle2D.Double(-iDmm/2,-length/2,iDmm, length)));
		
		return a;
	}


	public boolean isOuterSurfaceInhibited() {
		return outerSurfaceInhibited;
	}


	public void setOuterSurfaceInhibited(boolean outerSurfaceInhibited) throws PropertyVetoException {
		fireVetoableChange("outerSurfaceInhibited", this.outerSurfaceInhibited, outerSurfaceInhibited);
		boolean old = this.outerSurfaceInhibited;
		this.outerSurfaceInhibited = outerSurfaceInhibited;
		firePropertyChange("outerSurfaceInhibited", old, outerSurfaceInhibited);
	}


	public boolean isInnerSurfaceInhibited() {
		return innerSurfaceInhibited;
	}


	public void setInnerSurfaceInhibited(boolean innerSurfaceInhibited)  throws PropertyVetoException {
		fireVetoableChange("innerSurfaceInhibited", this.innerSurfaceInhibited, innerSurfaceInhibited);
		boolean old = this.innerSurfaceInhibited;
		this.innerSurfaceInhibited = innerSurfaceInhibited;
		firePropertyChange("innerSurfaceInhibited", old, innerSurfaceInhibited);
	}

	public static void main(String args[]) throws Exception {
		CoredCylindricalGrain e = new CoredCylindricalGrain();
		new Editor(e).show();
		new GrainPanel(e).show();
	}

	

}
