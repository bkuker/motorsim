import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import com.billkuker.rocketry.motorsim.visual.workbench.MotorWorkbench;

class Splash extends JWindow {
	private static final long serialVersionUID = 1L;

	public Splash(String resName, Frame f, int waitTime) {
		super(f);
		JLabel l = new JLabel(new ImageIcon(this.getClass()
				.getResource(resName)));
		add(l);
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension labelSize = l.getPreferredSize();
		setLocation(screenSize.width / 2 - (labelSize.width / 2),
				screenSize.height / 2 - (labelSize.height / 2));
		final int pause = waitTime;
		setVisible(true);
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(pause);
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							setVisible(false);
							dispose();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "SplashThread").start();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
		g.drawString("Version " + MotorWorkbench.version, 140, 150);
	}

	public static void main(String args[]) {
		 new Splash("splash.png", null, 1000);
	}
}
