package com.billkuker.rocketry.motorsim.grain;

import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Validating;
import com.billkuker.rocketry.motorsim.Validating.ValidationException;
import com.billkuker.rocketry.motorsim.grain.util.BurningShape;
import com.billkuker.rocketry.motorsim.grain.util.RotatedShapeGrain;

public class EndBurner extends RotatedShapeGrain implements Validating {

	private Amount<Length> length = Amount.valueOf(70, SI.MILLIMETER);
	private Amount<Length> oD = Amount.valueOf(30, SI.MILLIMETER);
	private Amount<Length> puntDiameter = Amount.valueOf(10, SI.MILLIMETER);
	private Amount<Length> puntDepth = Amount.valueOf(10, SI.MILLIMETER);
	
	private void generateGeometry(){
		double len = length.doubleValue(SI.MILLIMETER);
		double od = oD.doubleValue(SI.MILLIMETER);
		double pdi = puntDiameter.doubleValue(SI.MILLIMETER);
		double plen = puntDepth.doubleValue(SI.MILLIMETER);
		
		Rectangle2D.Double grain, punt, end;
		grain = new Rectangle2D.Double(0,0,od/2.0,len);
		punt = new Rectangle2D.Double(0,len-plen, pdi/2.0, plen);
		end = new Rectangle2D.Double(0,len,od,0);
		
		shape = new BurningShape();
		web = null;
		
		shape.add(grain);
		shape.inhibit(grain);
		shape.subtract(punt);
		shape.subtract(end);
	}
	
	public EndBurner(){
		this.addPropertyChangeListener(new PropertyChangeListener(){
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				generateGeometry();
			}});
		generateGeometry();
	}
	
	@Override
	public void validate() throws ValidationException {
		if ( oD.equals(Amount.ZERO) )
			throw new ValidationException(this, "Invalid oD");
		if ( getLength().equals(Amount.ZERO) )
			throw new ValidationException(this, "Invalid Length");
		if ( puntDiameter.isGreaterThan(oD) )
			throw new ValidationException(this, "puntDiameter > oD");
		if ( puntDepth.isGreaterThan(length) )
			throw new ValidationException(this, "puntDepth > length");
	}

	public Amount<Length> getLength() {
		return length;
	}

	public void setLength(Amount<Length> length) {
		this.length = length;
	}

	public Amount<Length> getOD() {
		return oD;
	}

	public void setOD(Amount<Length> oD) {
		this.oD = oD;
	}

	public Amount<Length> getPuntDiameter() {
		return puntDiameter;
	}

	public void setPuntDiameter(Amount<Length> puntDiameter) {
		this.puntDiameter = puntDiameter;
	}

	public Amount<Length> getPuntDepth() {
		return puntDepth;
	}

	public void setPuntDepth(Amount<Length> puntDepth) {
		this.puntDepth = puntDepth;
	}

}
