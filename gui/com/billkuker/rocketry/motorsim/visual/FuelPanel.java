package com.billkuker.rocketry.motorsim.visual;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Fuel;

public class FuelPanel extends JSplitPane {
	private static final long serialVersionUID = 1L;

	public FuelPanel(Fuel f) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		setName("Fuel");
		Chart<Pressure, Velocity> burnRate;
		try {
			burnRate = new Chart<Pressure, Velocity>(SI.MEGA(SI.PASCAL),
					SI.METERS_PER_SECOND, f, "burnRate");
		} catch (NoSuchMethodException e) {
			throw new Error(e);
		}
		burnRate.setDomain(burnRate.new IntervalDomain(Amount.valueOf(0, SI
				.MEGA(SI.PASCAL)), Amount.valueOf(11, SI.MEGA(SI.PASCAL)), 20));

		final JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		p.add(new Editor(f));
		try {
			p.add(new Editor(f.getCombustionProduct()));
		} catch (Exception e) {

		}

		setLeftComponent(p);
		setRightComponent(burnRate);
	}
}
