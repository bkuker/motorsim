package com.billkuker.rocketry.motorsim.motors.example;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.ConvergentDivergentNozzle;
import com.billkuker.rocketry.motorsim.CylindricalChamber;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.fuel.KNSU;
import com.billkuker.rocketry.motorsim.grain.ExtrudedShapeGrain;

public class CSlot extends Motor {
	public CSlot() {
		setName("C-Slot");
		setFuel(new KNSU());

		CylindricalChamber c = new CylindricalChamber();
		c.setLength(Amount.valueOf(150, SI.MILLIMETER));
		c.setID(Amount.valueOf(50, SI.MILLIMETER));
		setChamber(c);

		setGrain(new ExtrudedShapeGrain() {
			{
				try {
					Shape outside = new Ellipse2D.Double(0, 0, 30, 30);
					xsection.add(outside);
					xsection.inhibit(outside);
					xsection.subtract(new Rectangle2D.Double(12.5,13, 5, 30));
					setLength(Amount.valueOf(150, SI.MILLIMETER));
					setForeEndInhibited(false);
					setAftEndInhibited(false);
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		});

		ConvergentDivergentNozzle n = new ConvergentDivergentNozzle();
		n.setThroatDiameter(Amount.valueOf(7, SI.MILLIMETER));
		n.setExitDiameter(Amount.valueOf(10, SI.MILLIMETER));
		n.setEfficiency(.85);
		setNozzle(n);
	}
	
	public static void main(String args[]){
		new Burn(new CSlot());
	}

}
