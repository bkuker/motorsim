import javax.swing.UIManager;

public class MotorWorkbench {

	public static void main(String args[]) throws Exception {

		try {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty(
					"com.apple.mrj.application.apple.menu.about.name",
					"MotorWorkbench");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		new com.billkuker.rocketry.motorsim.visual.workbench.MotorWorkbench().setVisible(true);
	}

}
