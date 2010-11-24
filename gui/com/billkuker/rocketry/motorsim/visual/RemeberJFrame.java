package com.billkuker.rocketry.motorsim.visual;

import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

public class RemeberJFrame extends JFrame {
	private static final long serialVersionUID = 1L;


	public RemeberJFrame(int width, int height){
		setSize(width, height);
		restore();
		addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				positionChanged();
			}
			@Override
			public void componentMoved(ComponentEvent arg0) {
				positionChanged();
			}
			@Override
			public void componentHidden(ComponentEvent arg0) {}
			@Override
			public void componentShown(ComponentEvent arg0) {}
		});
	}
	
	private void positionChanged(){
		Rectangle r = getBounds();
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		boolean max = (getExtendedState() & JFrame.MAXIMIZED_BOTH)!=0;
		if ( !max ){
			prefs.putInt("w", r.width);
			prefs.putInt("h", r.height);
			prefs.putInt("x", r.x);
			prefs.putInt("y", r.y);
		}
		prefs.putInt("m", max?1:0);
	}
	
	private void restore(){
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		Rectangle r = new Rectangle(
					prefs.getInt("x", 0),
					prefs.getInt("y", 0),
					prefs.getInt("w", getSize().width),
					prefs.getInt("h", getSize().height)
					);
		this.setSize(r.width, r.height);
		setLocation(r.x, r.y);
		
		if ( prefs.getInt("m", 0) == 1 )
			setExtendedState( getExtendedState()|JFrame.MAXIMIZED_BOTH );
	}

	
	public static void main(String[] args){
		RemeberJFrame f = new RemeberJFrame(300, 300);
		f.setDefaultCloseOperation(EXIT_ON_CLOSE);
		f.show();
	}
}
