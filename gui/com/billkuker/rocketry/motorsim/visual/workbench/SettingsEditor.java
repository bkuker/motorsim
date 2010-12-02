package com.billkuker.rocketry.motorsim.visual.workbench;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.visual.Editor;

public class SettingsEditor extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private final JFrame f;

	public SettingsEditor(final JFrame f) {
		super(f, MotorWorkbench.name + " - Simulation Settings", true);
		this.f = f;
		setSize(320, 240);
		setContentPane(new Editor(Burn.getBurnSettings()));
	}

	@Override
	public void setVisible(boolean v) {
		int x = f.getLocation().x + f.getWidth() / 2 - getWidth() / 2;
		int y = f.getLocation().y + f.getHeight() / 2 - getHeight() / 2;
		setLocation(x, y);
		super.setVisible(v);
	}

	public static void main(String args[]) {
		JFrame f = new JFrame();
		f.setSize(1024, 768);
		f.setVisible(true);
		final SettingsEditor s = new SettingsEditor(f);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				s.setVisible(true);

			}
		});

	}
}
