package com.billkuker.rocketry.motorsim.fuel;

import java.awt.BorderLayout;
import java.util.HashMap;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.RocketScience;

public class MultiFuelChart extends JPanel implements FuelResolver.FuelsChangeListener {
	private static final long serialVersionUID = 1L;

	private XYSeriesCollection dataset = new XYSeriesCollection();

	private HashMap<Fuel, XYSeries> fuelToSeries = new HashMap<Fuel, XYSeries>();
	private Unit<Pressure> pressureUnit;
	private Unit<Velocity> rateUnit;
	
	public MultiFuelChart() {
		this.setLayout(new BorderLayout());
		pressureUnit = RocketScience.UnitPreference.getUnitPreference()
				.getPreferredUnit(SI.PASCAL);
		rateUnit = RocketScience.UnitPreference.getUnitPreference()
				.getPreferredUnit(SI.METERS_PER_SECOND);
		JFreeChart chart = ChartFactory.createXYLineChart(
				"", // Title
				pressureUnit.toString(), // x-axis Label
				rateUnit.toString(), // y-axis Label
				dataset, PlotOrientation.VERTICAL, // Plot Orientation
				true, // Show Legend
				true, // Use tool tips
				false // Configure chart to generate URLs?
				);
		add(new ChartPanel(chart));
		FuelResolver.addFuelsChangeListener(this);
		fuelsChanged();
	}

	public void addFuel(Fuel f) {
		XYSeries s = createSeries(f);
		fuelToSeries.put(f, s);
		dataset.addSeries(s);
	}

	private XYSeries createSeries(Fuel f) {
		double max = 11;
		double steps = 50;
		XYSeries s = new XYSeries(f.getName());
		for ( double dp = 0; dp <= max; dp += max/steps ){
			Amount<Pressure> p = Amount.valueOf(dp, SI.MEGA(SI.PASCAL));
			Amount<Velocity> r = f.burnRate(p);
			s.add(p.doubleValue(pressureUnit), r.doubleValue(rateUnit));
		}
		return s;
	}

	public void removeFuel(Fuel f) {
		XYSeries s = fuelToSeries.get(f);
		if (s == null)
			return;
		dataset.removeSeries(s);
	}

	@Override
	public void fuelsChanged() {
		for ( Fuel f : FuelResolver.getFuelMap().values() ){
			if ( !fuelToSeries.containsKey(f) ){
				addFuel(f);
			}
		}
		for ( Fuel f : fuelToSeries.keySet() ){
			if ( !FuelResolver.getFuelMap().values().contains(f) ){
				removeFuel(f);
			}
		}
	}
}
