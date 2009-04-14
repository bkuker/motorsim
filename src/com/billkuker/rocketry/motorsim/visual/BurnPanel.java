package com.billkuker.rocketry.motorsim.visual;

import java.awt.BorderLayout;
import java.text.NumberFormat;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Burn;

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
			main.setDividerLocation(.5);
			main.setResizeWeight(.5);
			
			add( main, BorderLayout.CENTER );
			
			add( new SL(), BorderLayout.SOUTH);
			
		} catch (NoSuchMethodException e){
			throw new Error(e);
		}
		

	}
	
	private class SL extends JSlider implements ChangeListener{
		private static final long serialVersionUID = 1L;
		private static final int STEPS = 300;
		public SL(){
			addChangeListener(this);
			setMinimum(0);
			setMaximum(STEPS);
			setValue(0);
		}
		
		@Override
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
	
	public void show(){
		JFrame f = new JFrame();
		f.setSize(1280,720);
		f.setLocation(0, 0);
		f.setContentPane(this);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setVisible(true);
	}
}
