package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.WindowConstants;

import com.billkuker.rocketry.motorsim.RocketScience.UnitPreference;
import com.billkuker.rocketry.motorsim.fuel.FuelsEditor;
import com.billkuker.rocketry.motorsim.visual.RememberJFrame;


public class MotorWorkbench extends RememberJFrame {
	public static final String version = "2.0 BETA";
	public static final String name = "MotorSim " + version;
	private static final long serialVersionUID = 1L;
	
	private MultiMotorThrustChart mb;
	private JFrame allBurns;
	
	private JFrame fuelEditorFrame = new RememberJFrame(800,600){
		private static final long serialVersionUID = 1L;
		{
			setSize(800, 600);
			add(fuelEditor = new FuelsEditor(this));
			JMenuBar b;
			setJMenuBar(b = new JMenuBar());
			b.add(fuelEditor.getMenu());
			setTitle(name + " - Fuel Editor");
		}
	};
	private FuelsEditor fuelEditor;
	
	private MotorsEditor motorsEditor;

	
	public MotorWorkbench() {
		super(1024, 768);
		setTitle(name);

		motorsEditor = new MotorsEditor(this);
		setContentPane(motorsEditor);
		
		addMenu();
		
		mb = new MultiMotorThrustChart();
		allBurns = new JFrame();
		allBurns.setTitle("All Burns");
		allBurns.setSize(800, 600);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		allBurns.add(mb);


		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		//setVisible(true);

	}

	private void addMenu() {

		setJMenuBar(new JMenuBar() {
			private static final long serialVersionUID = 1L;

			{
				add(motorsEditor.getMenu());
				
				add(new JMenu("Settings") {
					private static final long serialVersionUID = 1L;
					{
						ButtonGroup units = new ButtonGroup();
						JRadioButtonMenuItem sci = new JRadioButtonMenuItem(
								"SI");
						JRadioButtonMenuItem nonsci = new JRadioButtonMenuItem(
								"NonSI");
						units.add(sci);
						units.add(nonsci);
						sci.setSelected(UnitPreference.getUnitPreference() == UnitPreference.SI);
						nonsci.setSelected(UnitPreference.getUnitPreference() == UnitPreference.NONSI);
						sci.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								UnitPreference
										.setUnitPreference(UnitPreference.SI);
							}
						});
						nonsci.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								UnitPreference
										.setUnitPreference(UnitPreference.NONSI);
							}
						});
						add(sci);
						add(nonsci);
					}
				});
				add(new JMenu("View") {
					private static final long serialVersionUID = 1L;
					{
						add(new JMenuItem("Detach \"All Motors\" tabs") {
							private static final long serialVersionUID = 1L;
							{
								addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent arg0) {
										motorsEditor.detach();
									}
								});
							}
						});
						add(new JMenuItem("Show Fuel Editor") {
							private static final long serialVersionUID = 1L;
							{
								addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent arg0) {
										fuelEditorFrame.setVisible(true);
										fuelEditorFrame.toFront();
									}
								});
							}
						});
					}
				});
			}
		});
	}

	


	
}
