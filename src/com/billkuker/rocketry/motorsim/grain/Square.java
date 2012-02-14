package com.billkuker.rocketry.motorsim.grain;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Validating;
import com.billkuker.rocketry.motorsim.grain.util.BurningShape;
import com.billkuker.rocketry.motorsim.grain.util.ExtrudedShapeGrain;

public class Square extends ExtrudedShapeGrain implements Validating {
	private Amount<Length> oD = Amount.valueOf(30, SI.MILLIMETER);
	private Amount<Length> side = Amount.valueOf(10, SI.MILLIMETER);

	
	public Square(){
		try {
			setLength(Amount.valueOf(70, SI.MILLIMETER));
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		generateGeometry();
	}
	
	private void generateGeometry(){
		double odmm = oD.doubleValue(SI.MILLIMETER);
		double sidemm = side.doubleValue(SI.MILLIMETER);
		
		xsection = new BurningShape();
		Shape outside = new Ellipse2D.Double(-odmm/2.0, -odmm/2.0, odmm, odmm);
		xsection.add(outside);
		xsection.inhibit(outside);
		webThickness = null;
		
		Rectangle2D r = new Rectangle2D.Double(-sidemm/2.0, -sidemm/2.0, sidemm, sidemm);
		xsection.subtract(r);

	}
	
	public Amount<Length> getOD() {
		return oD;
	}

	public void setOD(Amount<Length> od) throws PropertyVetoException {
		if (od.equals(this.oD))
			return;
		this.oD = od;
		generateGeometry();
	}

	public Amount<Length> getCoreSide() {
		return side;
	}

	public void setCoreSide(Amount<Length> side) throws PropertyVetoException {
		if (side.equals(this.side))
			return;
		this.side = side;
		generateGeometry();
	}
	

	
	@Override
	public void validate() throws ValidationException{
		if ( side.equals(Amount.ZERO) )
			throw new ValidationException(this, "Invalid side");
		if ( oD.equals(Amount.ZERO) )
			throw new ValidationException(this, "Invalid oD");
		if ( getLength().equals(Amount.ZERO) )
			throw new ValidationException(this, "Invalid Length");
	}
}
