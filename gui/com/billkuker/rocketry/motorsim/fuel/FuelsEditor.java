package com.billkuker.rocketry.motorsim.fuel;

import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.fuel.editable.EditablePiecewiseLinearFuel;
import com.billkuker.rocketry.motorsim.fuel.editable.EditablePiecewiseSaintRobertFuel;
import com.billkuker.rocketry.motorsim.io.MotorIO;
import com.billkuker.rocketry.motorsim.visual.MultiObjectEditor;

public class FuelsEditor extends MultiObjectEditor<Fuel, AbstractFuelEditor> {

	private static final long serialVersionUID = 1L;
	
	private MultiFuelChart allFuels = new MultiFuelChart();

	private static int lIdx = 0;
	private static int sIdx = 0;
	public FuelsEditor(Frame frame) {
		super(frame, "Fuel");
		addTab("All Fuels", allFuels);
		
		addCreator(new ObjectCreator() {
			@Override
			public Fuel newObject() {
				EditablePiecewiseLinearFuel ret = new EditablePiecewiseLinearFuel();
				ret.setName("New Linear Fuel " + ++lIdx);
				return ret;
			}

			@Override
			public String getName() {
				return "Linear Fuel";
			}
		});
		addCreator(new ObjectCreator() {
			@Override
			public Fuel newObject() {
				EditablePiecewiseSaintRobertFuel ret = new EditablePiecewiseSaintRobertFuel();
				ret.setName("New StRobert Fuel " + ++sIdx);
				return ret;
			}

			@Override
			public String getName() {
				return "Saint Robert Fuel";
			}
		});
	}
	
	/*
	@Override
	protected void objectAdded(Fuel f, AbstractFuelEditor e){
		allFuels.addFuel(f);
	}
	
	@Override
	protected void objectRemoved(Fuel f, AbstractFuelEditor e){
		allFuels.removeFuel(f);
	}*/

	@Override
	public AbstractFuelEditor createEditor(Fuel o) {
		if ( o instanceof EditablePiecewiseLinearFuel ){
			return new LinearFuelEditor((EditablePiecewiseLinearFuel)o);
		} else if ( o instanceof EditablePiecewiseSaintRobertFuel ){
			return new SRFuelEditor((EditablePiecewiseSaintRobertFuel)o);
		}
		return null;
	}

	@Override
	protected Fuel loadFromFile(File f) throws IOException {
		Fuel fuel =  MotorIO.readFuel(new FileInputStream(f));
		return fuel;
	}

	@Override
	protected void saveToFile(Fuel o, File f) throws IOException {
		MotorIO.writeFuel(o, new FileOutputStream(f));
	}
	
	@SuppressWarnings("deprecation")
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
