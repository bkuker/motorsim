package com.billkuker.rocketry.motorsim.visual;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.ChangeListening;
import com.billkuker.rocketry.motorsim.Grain;

public class GrainPanel extends JPanel {
	private static Logger log = Logger.getLogger(GrainPanel.class);
			
	private static final long serialVersionUID = 1L;
	private Amount<Length> displayedRegression = Amount.valueOf(0, SI.MILLIMETER);
	private JLabel l = new JLabel();
	private Chart<Length,Area> area;
	Chart<Length, Volume> volume;
	private XC xc;
	private Grain grain;
	
	public GrainPanel(Grain g){
		super(new BorderLayout());

		grain = g;
		
		if ( g instanceof ChangeListening.Subject ){
			((ChangeListening.Subject)g).addPropertyChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					repaint();
					area.setDomain(area.new IntervalDomain(Amount.valueOf(0, SI.MILLIMETER), grain.webThickness()));
					volume.setDomain(volume.new IntervalDomain(Amount.valueOf(0, SI.MILLIMETER), grain.webThickness()));
				}
			});
		}


		try {

			area = new Chart<Length, Area>(
					SI.MILLIMETER,
					SI.MILLIMETER.pow(2).asType(Area.class),
					grain,
					"surfaceArea", "Regression", "Area");
			area.setDomain(area.new IntervalDomain(Amount.valueOf(0, SI.MILLIMETER), grain.webThickness()));
			
			volume = new Chart<Length, Volume>(
					SI.MILLIMETER,
					SI.MILLIMETER.pow(3).asType(Volume.class),
					grain,
					"volume", "Regression", "Volume");
			volume.setDomain(volume.new IntervalDomain(Amount.valueOf(0, SI.MILLIMETER), grain.webThickness()));

			area.setMaximumSize(new Dimension(200,100));
			volume.setMaximumSize(new Dimension(200,100));
			

		} catch (ClassCastException e) {
			log.error(e);
		} catch (NoSuchMethodException e) {
			log.error(e);
		}
		
		addComponents(
				xc = new XC(grain),
				new SL(),
				l,
				area,
				volume
		);
	}
	
	protected void addComponents(
			Component crossSection,
			Component slider,
			Component label,
			Component area,
			Component volume
	){
		
		JSplitPane v = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane h = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		JPanel graphics = new JPanel(new BorderLayout());
		graphics.add(crossSection, BorderLayout.CENTER);
		graphics.add(label, BorderLayout.NORTH);
		graphics.add(slider, BorderLayout.SOUTH);
	
		v.setTopComponent(h);
		v.setBottomComponent(area);
		h.setLeftComponent(graphics);
		h.setRightComponent(volume);
		add(v);
		
		h.resetToPreferredSizes();
		v.resetToPreferredSizes();

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
			grain = g;
			java.awt.geom.Area unburnt = grain.getSideView(Amount.valueOf(0, SI.MILLIMETER));
			
			Rectangle bounds = unburnt.getBounds();
			double max = bounds.getWidth();
			if ( bounds.getHeight() > max )
				max = bounds.getHeight();
			int w = (int)(bounds.getWidth() * 200.0 / max);
			if ( w < 40 )
				w = 40;
			
			Dimension sz = new Dimension(240+w, 250);
			setMinimumSize(sz);
			setPreferredSize(sz);
			setMaximumSize(sz);
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
				g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
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
		
		public void stateChanged(ChangeEvent e) {
			double r = ((SL)e.getSource()).getValue();

			setDisplayedRegression(grain.webThickness().divide(STEPS).times(r));
		}
	}
	
	public void showAsWindow(){
		JFrame f = new JFrame();
		f.setTitle(grain.getClass().getName());
		f.setSize(1024,600);
		f.setContentPane(this);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setVisible(true);
	}

}
