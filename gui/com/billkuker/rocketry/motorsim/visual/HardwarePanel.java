package com.billkuker.rocketry.motorsim.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.measure.unit.SI;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Chamber;
import com.billkuker.rocketry.motorsim.ChangeListening;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.Nozzle;
import com.billkuker.rocketry.motorsim.visual.workbench.MotorEditor;

public class HardwarePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	final Motor m;
	
	
	private PropertyChangeListener repainter = new PropertyChangeListener(){
		public void propertyChange(PropertyChangeEvent evt) {
			repaint();
		}
	};
	
	public HardwarePanel(Motor m){
		this.m = m;
		Nozzle nozzle = m.getNozzle();
		Chamber chamber = m.getChamber();
		if ( nozzle instanceof ChangeListening.Subject ){
			((ChangeListening.Subject)nozzle).addPropertyChangeListener(repainter);
		}
		if ( chamber instanceof ChangeListening.Subject ){
			((ChangeListening.Subject)chamber).addPropertyChangeListener(repainter);
		}
	}
	
	public void paint(Graphics g){
		super.paint(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.translate(10, 10);

		g2d.setColor(Color.black);
		
		Nozzle nozzle = m.getNozzle();
		Chamber chamber = m.getChamber();
		
		Shape c = chamber.chamberShape();
		
		Shape n = nozzle.nozzleShape(Amount.valueOf(c.getBounds().getWidth(), SI.MILLIMETER));
		
		
		Rectangle cb = c.getBounds();
		Rectangle nb = n.getBounds();
		double w, h;
		w = Math.max(cb.getWidth(), nb.getWidth());
		h = cb.getHeight() + nb.getHeight();
		
		double mw, mh;
		mw = getHeight() - 10;
		mh = getWidth() - 10;
		
		double sw, sh, s;
		sw = mw / w;
		sh = mh / h;
		s = Math.min(sw, sh);
		
		g2d.rotate(-Math.PI / 2);
		
		g2d.translate(0, -cb.getY() - 5);
		g2d.scale(s, s);
		g2d.translate(-(getHeight()/(s*2)), 0);
		
		g2d.setStroke(new BasicStroke(1));
		g2d.draw( c );
		
		g2d.translate(0, cb.getHeight());
		
		g2d.draw(n);
		
		Shape grain = m.getGrain().getSideView(Amount.valueOf(0, SI.MILLIMETER));
		Shape grain2 = m.getGrain().getSideView(Amount.valueOf(1, SI.MILLIMETER));
		Area burning = new Area(grain);
		burning.subtract(new Area(grain2));
		Rectangle gb = grain.getBounds();
		double x = -gb.getMaxX() +  gb.getWidth()/2.0;
		g2d.translate(x, -gb.getMaxY());
		g2d.draw(grain);
		g2d.setColor(Color.GRAY);
		g2d.fill(grain);
		g2d.setColor(Color.RED);
		g2d.fill(burning);
	}
	
	public void showAsWindow(){
		JFrame f = new JFrame();
		f.setSize(220,250);
		f.setContentPane(this);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setVisible(true);
	}
	
	public static void main(String args[]) throws Exception{
		Motor m = MotorEditor.defaultMotor();
		new HardwarePanel(m).showAsWindow();
	}
}
