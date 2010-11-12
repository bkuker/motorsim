package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.fuel.EditableFuel;
import com.billkuker.rocketry.motorsim.fuel.PiecewiseLinearFuel;
import com.billkuker.rocketry.motorsim.fuel.SaintRobertFuel;
import com.billkuker.rocketry.motorsim.io.MotorIO;
import com.billkuker.rocketry.motorsim.visual.FuelPanel;
import com.billkuker.rocketry.motorsim.visual.MultiObjectEditor;
import com.billkuker.rocketry.motorsim.visual.workbench.AbstractFuelEditor.EditablePSRFuel;

public class FuelsEditor extends MultiObjectEditor<Fuel, AbstractFuelEditor> {

	public FuelsEditor(Frame frame) {
		super(frame, "Fuel");
	}

	@Override
	public AbstractFuelEditor createEditor(Fuel o) {
		if ( o instanceof PiecewiseLinearFuel ){
			return new LinearFuelEditor((PiecewiseLinearFuel)o);
		} else if ( o instanceof EditableFuel ){
			return new SRFuelEditor(new EditablePSRFuel(SaintRobertFuel.Type.SI));
		}
		return null;
	}

	@Override
	public Fuel newObject() {
		return new PiecewiseLinearFuel();
	}

	@Override
	protected Fuel loadFromFile(File f) throws IOException {
		return MotorIO.readFuel(new FileInputStream(f));
	}

	@Override
	protected void saveToFile(Fuel o, File f) throws IOException {
		MotorIO.writeFuel(o, new FileOutputStream(f));
	}
	
	public static void main(String args[]){
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		FuelsEditor fe;
		f.add(fe = new FuelsEditor(f));
		JMenuBar b;
		f.setJMenuBar(b = new JMenuBar());
		b.add(fe.getMenu());
		f.setSize(1024, 768);
		f.show();
	}

}
