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
import com.billkuker.rocketry.motorsim.visual.BurnPanel;

public class PVC9 extends Motor {
	public PVC9(){
		setName("PVC9");
		setFuel(new KNSU());
		
		CylindricalChamber c = new CylindricalChamber();
		c.setLength(Amount.valueOf(200, SI.MILLIMETER));
		c.setID(Amount.valueOf(30, SI.MILLIMETER));
		setChamber(c);
		
		CoredCylindricalGrain g = new CoredCylindricalGrain();
		try{
			g.setLength(Amount.valueOf(70, SI.MILLIMETER));
			g.setOD(Amount.valueOf(29, SI.MILLIMETER));
			g.setID(Amount.valueOf(8, SI.MILLIMETER));
		} catch ( PropertyVetoException v ){
			throw new Error(v);
		}
	
		setGrain( new MultiGrain(g, 2) );
		
		ConvergentDivergentNozzle n = new ConvergentDivergentNozzle();
		n.setThroatDiameter(Amount.valueOf(7.9, SI.MILLIMETER));
		n.setExitDiameter(Amount.valueOf(9, SI.MILLIMETER));
		n.setEfficiency(.87);
		setNozzle(n);
	}
	
	public static void main(String args[]) throws Exception{
		PVC9 m = new PVC9();
		Burn b = new Burn(m);
		new BurnPanel(b).show();
	}
}
