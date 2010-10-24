package com.billkuker.rocketry.motorsim.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.measure.unit.SI;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Chamber;
import com.billkuker.rocketry.motorsim.ChangeListening;
import com.billkuker.rocketry.motorsim.ConvergentDivergentNozzle;
import com.billkuker.rocketry.motorsim.CylindricalChamber;
import com.billkuker.rocketry.motorsim.Nozzle;

public class HardwarePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Nozzle nozzle;
	private Chamber chamber;
	
	public HardwarePanel(Nozzle n, Chamber c){
		nozzle = n;
		chamber = c;
		if ( n instanceof ChangeListening.Subject ){
			((ChangeListening.Subject)n).addPropertyChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					repaint();
				}
			});
		}
		if ( c instanceof ChangeListening.Subject ){
			((ChangeListening.Subject)c).addPropertyChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					repaint();
				}
			});
		}
	}
	
	public void paint(Graphics g){
		super.paint(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.translate(10, 10);

		g2d.setColor(Color.black);
		
		Shape c = chamber.chamberShape();
		
		Shape n = nozzle.nozzleShape(((CylindricalChamber)chamber).getID());
		
		Rectangle cb = c.getBounds();
		Rectangle nb = n.getBounds();
		double w, h;
		w = Math.max(cb.getWidth(), nb.getWidth());
		h = cb.getHeight() + nb.getHeight();
		
		double mw, mh;
		mw = getWidth() - 10;
		mh = getHeight() - 10;
		
		double sw, sh, s;
		sw = mw / w;
		sh = mh / h;
		s = Math.min(sw, sh);
		
		g2d.translate(0, -cb.getY() - 5);
		g2d.scale(s, s);
		g2d.translate(-cb.getX(), 0);
		
		g2d.setStroke(new BasicStroke(1));
		g2d.draw( c );
		g2d.translate(0, cb.getHeight());
		
		g2d.draw(n);
	}
	
	public void showAsWindow(){
		JFrame f = new JFrame();
		f.setSize(220,250);
		f.setContentPane(this);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setVisible(true);
	}
	
	public static void main(String args[]) throws Exception{
		ConvergentDivergentNozzle n = new ConvergentDivergentNozzle();
		CylindricalChamber c = new CylindricalChamber();
		n.setThroatDiameter(Amount.valueOf(10, SI.MILLIMETER));
		n.setExitDiameter(Amount.valueOf(20, SI.MILLIMETER));
		//new Editor(n).showAsWindow();
		new HardwarePanel(n,c).showAsWindow();
	}
}
