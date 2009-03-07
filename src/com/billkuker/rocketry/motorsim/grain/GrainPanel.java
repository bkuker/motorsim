package com.billkuker.rocketry.motorsim.grain;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.visual.Chart;

public class GrainPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Amount<Length> displayedRegression = Amount.valueOf(0, SI.MILLIMETER);
	private JLabel l = new JLabel();
	private Chart<Length,Area> area;
	Chart<Length, Volume> volume;
	private XC xc;
	private SL sl;
	private Grain grain;
	
	public GrainPanel(Grain g){
		super(new BorderLayout());

		grain = g;

		try {

			area = new Chart<Length, Area>(
					SI.MILLIMETER,
					SI.MILLIMETER.pow(2).asType(Area.class),
					grain,
					"surfaceArea");
			area.setDomain(area.new IntervalDomain(Amount.valueOf(0, SI.MILLIMETER), grain.webThickness()));
			
			volume = new Chart<Length, Volume>(
					SI.MILLIMETER,
					SI.MILLIMETER.pow(3).asType(Volume.class),
					grain,
					"volume");
			volume.setDomain(volume.new IntervalDomain(Amount.valueOf(0, SI.MILLIMETER), grain.webThickness()));

			area.setMaximumSize(new Dimension(200,100));
			volume.setMaximumSize(new Dimension(200,100));
			

		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		JSplitPane charts = new JSplitPane(JSplitPane.VERTICAL_SPLIT, area, volume);
		charts.setDividerLocation(.5);
		
		
		if ( grain instanceof Grain.Graphical)
			add(xc = new XC((Grain.Graphical)grain), BorderLayout.CENTER);
		
		JPanel left = new JPanel(new BorderLayout());
		left.add(xc);
		left.add(l, BorderLayout.NORTH);
		left.add( new SL(), BorderLayout.SOUTH);
	
		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, charts));

	}
	
	private class XC extends JPanel{
		private static final long serialVersionUID = 1L;
		Grain.Graphical grain;
		public XC(Grain.Graphical g){
			setMinimumSize(new Dimension(120,120));
			grain = g;
		}
		public void paint(Graphics g){
			super.paint(g);
			Graphics2D g2d = (Graphics2D)g;
			g2d.translate(10, 30);
			grain.draw(g2d, displayedRegression );
		}
	}
	
	private class SL extends JSlider implements ChangeListener{
		private static final long serialVersionUID = 1L;
		private static final int STEPS = 20;
		public SL(){
			addChangeListener(this);
			setMinimum(0);
			setMaximum(STEPS);
			setValue(0);
		}
		
		@Override
		public void stateChanged(ChangeEvent e) {
			double r = ((SL)e.getSource()).getValue();
			displayedRegression = grain.webThickness().divide(STEPS).times(r);
			l.setText("Regression: " + displayedRegression);
			area.mark(displayedRegression);
			volume.mark(displayedRegression);
			if ( xc != null )
				xc.repaint();
		}
	}
	
	public void show(){
		JFrame f = new JFrame();
		f.setSize(1024,600);
		f.setContentPane(this);
		f.setDefaultCloseOperation(f.DISPOSE_ON_CLOSE);
		f.setVisible(true);
	}

}
