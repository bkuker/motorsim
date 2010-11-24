package com.billkuker.rocketry.motorsim.visual;

import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

public class RememberJFrame extends JFrame {
	private static final long serialVersionUID = 1L;


	public RememberJFrame(int width, int height){
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
	
	protected String getPositionKey(){
		return this.getClass().getName();
	}
	
	private void positionChanged(){
		Rectangle r = getBounds();
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		boolean max = (getExtendedState() & JFrame.MAXIMIZED_BOTH)!=0;
		if ( !max ){
			prefs.putInt(getPositionKey() + ".w", r.width);
			prefs.putInt(getPositionKey() + ".h", r.height);
			prefs.putInt(getPositionKey() + ".x", r.x);
			prefs.putInt(getPositionKey() + ".y", r.y);
		}
		prefs.putInt("m", max?1:0);
	}
	
	private void restore(){
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		Rectangle r = new Rectangle(
					prefs.getInt(getPositionKey() + ".x", 0),
					prefs.getInt(getPositionKey() + ".y", 0),
					prefs.getInt(getPositionKey() + ".w", getSize().width),
					prefs.getInt(getPositionKey() + ".h", getSize().height)
					);
		this.setSize(r.width, r.height);
		setLocation(r.x, r.y);
		
		if ( prefs.getInt("m", 0) == 1 )
			setExtendedState( getExtendedState()|JFrame.MAXIMIZED_BOTH );
	}

	
	@SuppressWarnings("deprecation")
	public static void main(String[] args){
		RememberJFrame f = new RememberJFrame(300, 300);
		f.setDefaultCloseOperation(EXIT_ON_CLOSE);
		f.show();
	}
}
