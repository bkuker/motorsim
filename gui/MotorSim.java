import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.billkuker.rocketry.motorsim.visual.workbench.MotorWorkbench;

public class MotorSim {

	public static void main(String args[]) throws Exception {

		try {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty(
					"com.apple.mrj.application.apple.menu.about.name",
					"MotorSim");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		new Splash("splash.png",null, 2000);
		final MotorWorkbench mw = new com.billkuker.rocketry.motorsim.visual.workbench.MotorWorkbench();
		Thread.sleep(2000);
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				mw.setVisible(true);
			}
		});
		
		
	}

}
