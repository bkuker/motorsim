package com.billkuker.rocketry.motorsim.grain;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.text.NumberFormat;

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
		charts.setResizeWeight(.5);
		
		JPanel left = new JPanel(new BorderLayout());
		
		add(xc = new XC(grain), BorderLayout.CENTER);
		left.add(xc);

		left.add(l, BorderLayout.NORTH);
		left.add( sl = new SL(), BorderLayout.SOUTH);
	
		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, charts));

	}
	
	public void setDisplayedRegression( Amount<Length> r ){
		displayedRegression = r;
		
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		l.setText("Regression: " + nf.format(displayedRegression.doubleValue(SI.MILLIMETER)) + "mm");
		
		area.mark(displayedRegression);
		volume.mark(displayedRegression);
		if ( xc != null )
			xc.repaint();
	}
	
	private class XC extends JPanel{
		private static final long serialVersionUID = 1L;
		Grain grain;
		public XC(Grain g){
			setMinimumSize(new Dimension(440,250));
			grain = g;
		}
		public void paint(Graphics g){
			super.paint(g);
			Graphics2D g2d = (Graphics2D)g;
			g2d.translate(10, 30);
			/*
			grain.draw(g2d, displayedRegression );
			*/
			
			{
				AffineTransform t = g2d.getTransform();
				java.awt.geom.Area unburnt = grain.getCrossSection(Amount.valueOf(0, SI.MILLIMETER));
				
				Rectangle bounds = unburnt.getBounds();
				g2d.scale(200 / bounds.getWidth(), 200 / bounds.getHeight());
				g2d.translate(-bounds.getX(), -bounds.getY());
	
				//Draw the fuel that is left
				java.awt.geom.Area burning = grain.getCrossSection(displayedRegression);
				g2d.setColor(Color.RED);
				g2d.fill(burning);
				//Draw the fuel that is left
				java.awt.geom.Area left = grain.getCrossSection(displayedRegression.plus(grain.webThickness().divide(30)));
				g2d.setColor(Color.GRAY);
				g2d.fill(left);
				//Draw the outline of the unburnt grain
				g2d.setColor(Color.BLACK);
				g2d.draw(unburnt);
				//untranslate
				g2d.setTransform(t);
			}
			{
				AffineTransform t = g2d.getTransform();
				java.awt.geom.Area unburnt = grain.getSideView(Amount.valueOf(0, SI.MILLIMETER));
				
				Rectangle bounds = unburnt.getBounds();
				g2d.translate(220, 0);
				
				double max = bounds.getWidth();
				if ( bounds.getHeight() > max )
					max = bounds.getHeight();
				
				g2d.scale(200 / max, 200 / max);
				g2d.translate(-bounds.getX(), -bounds.getY());
	
				//Draw the fuel that is left
				java.awt.geom.Area burning = grain.getSideView(displayedRegression);
				g2d.setColor(Color.RED);
				g2d.fill(burning);
				//Draw the fuel that is left
				java.awt.geom.Area left = grain.getSideView(displayedRegression.plus(grain.webThickness().divide(30)));
				g2d.setColor(Color.GRAY);
				g2d.fill(left);
				//Draw the outline of the unburnt grain
				g2d.setColor(Color.BLACK);
				g2d.draw(unburnt);
				//untranslate
				g2d.setTransform(t);
			}
			
		}
	}
	
	private class SL extends JSlider implements ChangeListener{
		private static final long serialVersionUID = 1L;
		private static final int STEPS = 60;
		public SL(){
			addChangeListener(this);
			setMinimum(0);
			setMaximum(STEPS);
			setValue(0);
		}
		
		@Override
		public void stateChanged(ChangeEvent e) {
			double r = ((SL)e.getSource()).getValue();

			setDisplayedRegression(grain.webThickness().divide(STEPS).times(r));
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
