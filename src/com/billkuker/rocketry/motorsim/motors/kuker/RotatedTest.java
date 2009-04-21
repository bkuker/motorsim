package com.billkuker.rocketry.motorsim.motors.kuker;

import java.beans.PropertyVetoException;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.ConvergentDivergentNozzle;
import com.billkuker.rocketry.motorsim.CylindricalChamber;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.fuel.KNSU;
import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;
import com.billkuker.rocketry.motorsim.grain.MultiGrain;
import com.billkuker.rocketry.motorsim.grain.RotatedShapeGrain;
import com.billkuker.rocketry.motorsim.visual.BurnPanel;

public class RotatedTest extends Motor {
	public RotatedTest(){
		setName("R-Test");
		setFuel(new KNSU());
		
		CylindricalChamber c = new CylindricalChamber();
		c.setLength(Amount.valueOf(200, SI.MILLIMETER));
		c.setID(Amount.valueOf(25, SI.MILLIMETER));
		setChamber(c);
		
	
		setGrain( RotatedShapeGrain.DEFAULT_GRAIN );
		
		ConvergentDivergentNozzle n = new ConvergentDivergentNozzle();
		n.setThroatDiameter(Amount.valueOf(5.962, SI.MILLIMETER));
		n.setExitDiameter(Amount.valueOf(13.79, SI.MILLIMETER));
		n.setEfficiency(.85);
		setNozzle(n);
	}
	
	public static void main(String args[]) throws Exception{
		RotatedTest m = new RotatedTest();
		Burn b = new Burn(m);
		new BurnPanel(b).showAsWindow();
	}
}
