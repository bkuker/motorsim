package com.billkuker.rocketry.motorsim.motors.example;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.ConvergentDivergentNozzle;
import com.billkuker.rocketry.motorsim.CylindricalChamber;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.fuel.KNSU;
import com.billkuker.rocketry.motorsim.grain.util.RotatedShapeGrain;

public class EndBurner extends Motor {
	public EndBurner() {
		setName("End Burner");
		setFuel(new KNSU());

		CylindricalChamber c = new CylindricalChamber();
		c.setLength(Amount.valueOf(150, SI.MILLIMETER));
		c.setID(Amount.valueOf(50, SI.MILLIMETER));
		setChamber(c);

		setGrain(new RotatedShapeGrain() {
			{
				try {
					Shape outside = new Rectangle2D.Double(0, 0, 15, 70);
					shape.add(outside);
					shape.inhibit(outside);
					shape.subtract(new Rectangle2D.Double(0, 50, 5, 70));
					shape.subtract(new Rectangle2D.Double(0, 70, 15, 10));
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		});

		ConvergentDivergentNozzle n = new ConvergentDivergentNozzle();
		n.setThroatDiameter(Amount.valueOf(4, SI.MILLIMETER));
		n.setExitDiameter(Amount.valueOf(9, SI.MILLIMETER));
		n.setEfficiency(.85);
		setNozzle(n);
	}

}
