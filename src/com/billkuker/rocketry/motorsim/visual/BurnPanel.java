package com.billkuker.rocketry.motorsim.visual;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.NumberFormat;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.RocketScience;
import com.billkuker.rocketry.motorsim.Burn.Interval;

public class BurnPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Burn burn;
	Chart<Duration, Pressure> pressure;
	Chart<Duration, Force> thrust;
	Chart<Pressure, Velocity> burnRate;
	GrainPanel grain;
	Amount<Duration> displayedTime = Amount.valueOf(0, SI.SECOND);
	
	public BurnPanel(Burn b){
		super( new BorderLayout() );
		burn = b;
		
		try {
			pressure = new Chart<Duration, Pressure>(
					SI.SECOND,
					SI.MEGA(SI.PASCAL),
					b,
					"pressure");
			pressure.setDomain(burn.getData().keySet());
			
			thrust = new Chart<Duration, Force>(
					SI.SECOND,
					NonSI.POUND_FORCE,//SI.NEWTON,
					b,
					"thrust");
			thrust.setDomain(burn.getData().keySet());
			
			burnRate = new Chart<Pressure, Velocity>(
					SI.MEGA(SI.PASCAL),
					SI.METERS_PER_SECOND,
					burn.getMotor().getFuel(),
					"burnRate");
			burnRate.setDomain(
					burnRate.new IntervalDomain(
							Amount.valueOf(0, SI.MEGA(SI.PASCAL)),
							Amount.valueOf(11, SI.MEGA(SI.PASCAL)),
							20
							));
			
			
			JSplitPane tp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, thrust, pressure);
			tp.setDividerLocation(.5);
			tp.setResizeWeight(.5);
			
			grain = new GrainPanel(burn.getMotor().getGrain());
			
			JSplitPane grains = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, grain, burnRate);
			grains.setDividerLocation(.5);
			grains.setResizeWeight(.5);
			
			JSplitPane main = new JSplitPane(JSplitPane.VERTICAL_SPLIT, grains, tp);
			Dimension minimumSize = new Dimension(800, 200);
			grains.setMinimumSize(minimumSize);
			tp.setMinimumSize(minimumSize);
			main.setDividerLocation(.5);
			main.setResizeWeight(.5);
			
			add( main, BorderLayout.CENTER );
			
			add( new SL(), BorderLayout.SOUTH);
			
			

				Amount<RocketScience.Impulse> ns = Amount.valueOf(0, RocketScience.NEWTON_SECOND);
				Amount<Force> averageThrust = Amount.valueOf(0, SI.NEWTON);
				Amount<Force> maxThrust = Amount.valueOf(0, SI.NEWTON);
				Amount<Pressure> maxPressure = Amount.valueOf(0, SI.MEGA(SI.PASCAL));
				int thrustCount = 0;
				
				for( Interval i: burn.getData().values() ){
					ns = ns.plus(i.dt.times(i.thrust));
					if ( i.thrust.isGreaterThan(Amount.valueOf(0.01, SI.NEWTON))){
						thrustCount++;
						averageThrust = averageThrust.plus(i.thrust);
					}
					if ( i.thrust.isGreaterThan(maxThrust))
						maxThrust = i.thrust;
					if ( i.chamberPressure.isGreaterThan(maxPressure))
						maxPressure = i.chamberPressure;
				}
				averageThrust = averageThrust.divide(thrustCount);

				int cn = (int)(Math.log(ns.doubleValue(RocketScience.NEWTON_SECOND)/1.25) / Math.log(2));
				char cl = (char)((int)'A' + cn);

			
			JPanel text = new JPanel(new GridLayout(2,5));

			text.add(new JLabel("Rating"));
			text.add(new JLabel("Total Impulse"));
			text.add(new JLabel("Max Thrust"));
			text.add(new JLabel("Average Thust"));
			text.add(new JLabel("Max Pressure"));
			
			text.add(new JLabel(new String(new char[]{cl}) + "-" +Math.round(averageThrust.doubleValue(SI.NEWTON))));
			text.add(new JLabel(approx(ns)));
			text.add(new JLabel(approx(maxThrust)));
			text.add(new JLabel(approx(averageThrust)));
			text.add(new JLabel(approx(maxPressure)));
			
			add(text, BorderLayout.NORTH);
			
			
		} catch (NoSuchMethodException e){
			throw new Error(e);
		}
		

	}
	
	private String approx(Amount a){
		double d = a.doubleValue(a.getUnit());
		long i = Math.round(d);
		return i + " " + a.getUnit().toString();
	}
	
	private class SL extends JSlider implements ChangeListener{
		private static final long serialVersionUID = 1L;
		private static final int STEPS = 80;
		public SL(){
			addChangeListener(this);
			setMinimum(0);
			setMaximum(STEPS);
			setValue(0);
		}
		
		public void stateChanged(ChangeEvent e) {
			double t = ((SL)e.getSource()).getValue();
			displayedTime = burn.burnTime().divide(STEPS).times(t);
			
			//Find the nearest key in the data set
			displayedTime =burn.getData().tailMap(displayedTime).firstKey();
			
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);
			//System.out.println("Time: " + nf.format(displayedTime.doubleValue(SI.SECOND)) + "s");
			
			pressure.mark(displayedTime);
			thrust.mark(displayedTime);
			
			grain.setDisplayedRegression(burn.getData().get(displayedTime).regression);
			
			burnRate.mark(burn.getData().get(displayedTime).chamberPressure);
			
			
			/*
			double r = ((SL)e.getSource()).getValue();
			displayedRegression = grain.webThickness().divide(STEPS).times(r);
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);
			l.setText("Regression: " + nf.format(displayedRegression.doubleValue(SI.MILLIMETER)) + "mm");
			area.mark(displayedRegression);
			volume.mark(displayedRegression);
			if ( xc != null )
				xc.repaint();
			*/
		}
	}
	
	public void showAsWindow(){
		JFrame f = new JFrame();
		f.setTitle(burn.getMotor().getName());
		f.setSize(1280,720);
		f.setLocation(0, 0);
		f.setContentPane(this);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setVisible(true);
	}
}
