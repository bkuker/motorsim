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

import com.billkuker.rocketry.motorsim.ConvergentDivergentNozzle;
import com.billkuker.rocketry.motorsim.MotorPart;
import com.billkuker.rocketry.motorsim.Nozzle;

public class NozzlePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Nozzle nozzle;
	public NozzlePanel(Nozzle n){
		nozzle = n;
		
		if ( n instanceof MotorPart ){
			((MotorPart)n).addPropertyChangeListener(new PropertyChangeListener(){
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
		
		
		Shape a = nozzle.nozzleShape(Amount.valueOf(30, SI.MILLIMETER));
		
		Rectangle bounds = a.getBounds();
		double max = bounds.getWidth();
		if ( bounds.getHeight() > max )
			max = bounds.getHeight();
		
		g2d.scale(200 / max, 200 / max);
		g2d.translate(-bounds.getX(), -bounds.getY());
		
		g2d.setStroke(new BasicStroke(4*(float)max/200f));
		
		g2d.draw( a );
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
		n.setThroatDiameter(Amount.valueOf(5, SI.MILLIMETER));
		n.setExitDiameter(Amount.valueOf(9, SI.MILLIMETER));
		new Editor(n).show();
		new NozzlePanel(n).show();
	}
}
