package com.billkuker.rocketry.motorsim.grain;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.beans.PropertyVetoException;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Validating;
import com.billkuker.rocketry.motorsim.grain.util.BurningShape;
import com.billkuker.rocketry.motorsim.grain.util.ExtrudedShapeGrain;

public class Star extends ExtrudedShapeGrain implements Validating {
	private Amount<Length> oD = Amount.valueOf(30, SI.MILLIMETER);
	private Amount<Length> iD = Amount.valueOf(5, SI.MILLIMETER);
	private Amount<Length> pD = Amount.valueOf(15, SI.MILLIMETER);
	private int pointCount = 5;
	
	public Star(){
		try {
			setLength(Amount.valueOf(70, SI.MILLIMETER));
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		generateGeometry();
	}
	
	private void generateGeometry(){
		double odmm = oD.doubleValue(SI.MILLIMETER);
		double idmm = iD.doubleValue(SI.MILLIMETER)/2.0;
		double pdmm = pD.doubleValue(SI.MILLIMETER)/2.0;
		
		xsection = new BurningShape();
		Shape outside = new Ellipse2D.Double(-odmm/2, -odmm/2, odmm, odmm);
		xsection.add(outside);
		xsection.inhibit(outside);
		//xsection.subtract(new Ellipse2D.Double(-idmm/2, -idmm/2, idmm, idmm));
		webThickness = null;
		
		GeneralPath p = new GeneralPath();
		double theta = 0;
		double dTheta = (2.0 * Math.PI) / ( pointCount * 2.0 );
		p.moveTo(0, idmm);
		for ( int i = 0; i < pointCount; i++ ){
			theta += dTheta;
			p.lineTo(pdmm*Math.sin(theta), pdmm*Math.cos(theta));
			theta += dTheta;
			p.lineTo(idmm*Math.sin(theta), idmm*Math.cos(theta));
		}
		p.closePath();
		xsection.subtract(new Area(p));
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

	public Amount<Length> getID() {
		return iD;
	}

	public void setID(Amount<Length> id) throws PropertyVetoException {
		if (id.equals(this.iD))
			return;
		iD = id;
		generateGeometry();
	}
	
	public Amount<Length> getPointDiameter() {
		return pD;
	}

	public void setPointDiameter(Amount<Length> pd) throws PropertyVetoException {
		if (pd.equals(this.pD))
			return;
		pD = pd;
		generateGeometry();
	}
	
	public int getPointCount(){
		return pointCount;
	}
	
	public void setPointCount(final int points) throws PropertyVetoException{
		if ( points < 2 || points > 8 )
			throw new PropertyVetoException("Invalid number of points", null);
		pointCount = points;
		generateGeometry();
	}
	
	@Override
	public void validate() throws ValidationException{
		if ( iD.equals(Amount.ZERO) )
			throw new ValidationException(this, "Invalid iD");
		if ( oD.equals(Amount.ZERO) )
			throw new ValidationException(this, "Invalid oD");
		if ( getLength().equals(Amount.ZERO) )
			throw new ValidationException(this, "Invalid Length");
		if ( iD.isGreaterThan(oD) )
			throw new ValidationException(this, "iD > oD");		
		if ( iD.isGreaterThan(pD) )
			throw new ValidationException(this, "iD > pD");		
		if ( pD.isGreaterThan(oD) )
			throw new ValidationException(this, "pD > oD");	
	}
}
