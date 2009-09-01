package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.BorderLayout;
import java.util.HashMap;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.ConvergentDivergentNozzle;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.RocketScience;

public class MultiBurnChart extends JPanel implements BurnWatcher {
	private static final long serialVersionUID = 1L;

	private XYSeriesCollection dataset = new XYSeriesCollection();

	private HashMap<Burn, XYSeries> burnToSeries = new HashMap<Burn, XYSeries>();
	private Unit<Duration> time;
	private Unit<Force> force;

	@SuppressWarnings("unchecked")
	public MultiBurnChart() {
		this.setLayout(new BorderLayout());
		time = RocketScience.UnitPreference.preference
				.getPreferredUnit(SI.SECOND);
		force = RocketScience.UnitPreference.preference
				.getPreferredUnit(SI.NEWTON);
		JFreeChart chart = ChartFactory.createXYLineChart(
				"", // Title
				time.toString(), // x-axis Label
				force.toString(), // y-axis Label
				dataset, PlotOrientation.VERTICAL, // Plot Orientation
				true, // Show Legend
				true, // Use tool tips
				false // Configure chart to generate URLs?
				);
		add(new ChartPanel(chart));
	}

	public void addBurn(Burn b) {
		XYSeries s = createSeries(b);
		burnToSeries.put(b, s);
		dataset.addSeries(s);
	}

	private XYSeries createSeries(Burn b) {
		XYSeries s = new XYSeries(b.getMotor().getName());
		for( Burn.Interval i : b.getData().values() ){
			s.add(i.time.doubleValue(time), i.thrust.doubleValue(force));
		}
		return s;
	}

	public void removeBurn(Burn b) {
		XYSeries s = burnToSeries.get(b);
		if (s == null)
			return;
		dataset.removeSeries(s);
	}
	
	public static void main(String args[]) throws Exception{
		MultiBurnChart c = new MultiBurnChart();
		
		JFrame f = new JFrame();
		f.setSize(1024, 768);
		f.setContentPane(c);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setVisible(true);

		Motor m = MotorEditor.defaultMotor();
		Burn b = new Burn(m);
		c.addBurn(b);
		
		m.setName("Motor2");
		((ConvergentDivergentNozzle)m.getNozzle()).setThroatDiameter(Amount.valueOf(3, SI.MILLIMETER));
		c.addBurn(new Burn(m));
		
		Thread.sleep(5000);
		
		c.removeBurn(b);
		
	}

	@Override
	public void replace(Burn oldBurn, Burn newBurn) {
		removeBurn(oldBurn);
		addBurn(newBurn);
	}
}
