package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;

import com.billkuker.rocketry.motorsim.RocketScience.UnitPreference;
import com.billkuker.rocketry.motorsim.debug.DebugFrame;
import com.billkuker.rocketry.motorsim.fuel.FuelsEditor;
import com.billkuker.rocketry.motorsim.visual.RememberJFrame;


public class MotorWorkbench extends RememberJFrame {
	public static final String version = "2.0 BETA3";
	public static final String name = "MotorSim " + version;
	private static final long serialVersionUID = 1L;
	
	private MultiMotorThrustChart mb;
	private JFrame allBurns;
	
	private SettingsEditor settings = new SettingsEditor(this);
	
	private About about = new About(this);
	
	private JFrame fuelEditorFrame = new RememberJFrame(800,600){
		private static final long serialVersionUID = 1L;
		{
			setIconImage(getIcon());
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

	public static Image getIcon(){
		return Toolkit.getDefaultToolkit().getImage(MotorWorkbench.class.getResource("icon.png"));
	}
	
	public MotorWorkbench() {
		super(1024, 768);
		setTitle(name);
		setIconImage(getIcon());

		motorsEditor = new MotorsEditor(this);
		setContentPane(motorsEditor);
		
		addMenu();
		
		mb = new MultiMotorThrustChart();
		allBurns = new JFrame();
		allBurns.setTitle("All Burns");
		allBurns.setSize(800, 600);
		allBurns.add(mb);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {}
			
			@Override
			public void windowIconified(WindowEvent e) {}
			
			@Override
			public void windowDeiconified(WindowEvent e) {}
			
			@Override
			public void windowDeactivated(WindowEvent e) {}
			
			@Override
			public void windowClosing(WindowEvent e) {
				maybeQuit();
			}
			
			@Override
			public void windowClosed(WindowEvent e) {}
			
			@Override
			public void windowActivated(WindowEvent e) {}
		});

	}
	
	private void maybeQuit(){
		if (motorsEditor.hasDirty()) {
			int response = JOptionPane
					.showConfirmDialog(
							MotorWorkbench.this,
							"There are unsaved Motors.\nExit Anyway?",
							"Confirm",
							JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.NO_OPTION) {
				return;
			}
		}
		if (fuelEditor.hasDirty()) {
			int response = JOptionPane
					.showConfirmDialog(
							MotorWorkbench.this,
							"There are unsaved Fuels.\nExit Anyway?",
							"Confirm",
							JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.NO_OPTION) {
				return;
			}
		}
		MotorWorkbench.this.dispose();
		System.exit(0);
	}

	private void addMenu() {

		setJMenuBar(new JMenuBar() {
			private static final long serialVersionUID = 1L;

			{
				JMenu file = motorsEditor.getMenu();
				file.add(new JSeparator());
				file.add(new JMenuItem("Quit") {
					private static final long serialVersionUID = 1L;
					{
						addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								maybeQuit();
							}
						});
					}
				});
				add(file);
				
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
						
						add(new JSeparator());
						add(new JMenuItem("Simulation Settings"){
							private static final long serialVersionUID = 1L;
							{
								addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										settings.setVisible(true);
									}
								});
							}
						});
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
				add(new JMenu("Help"){
					private static final long serialVersionUID = 1L;
					{
						add(new JMenuItem("About") {
							private static final long serialVersionUID = 1L;
							{
								addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										about.setVisible(true);
									}
								});
							}
						});
						add(new JSeparator());
						add(new JMenu("Debug"){
							private static final long serialVersionUID = 1L;
							{
								add(new JMenuItem("Debug Window") {
									private static final long serialVersionUID = 1L;
									{
										addActionListener(new ActionListener() {
											@Override
											public void actionPerformed(ActionEvent e) {
												new DebugFrame();
											}
										});
									}
								});
								add(new JMenuItem("Log Window") {
									LF5Appender lf5;
									private static final long serialVersionUID = 1L;
									{
										addActionListener(new ActionListener() {
											@Override
											public void actionPerformed(ActionEvent e) {
												if ( lf5 == null ){
													lf5 = new LF5Appender();
													Logger.getRootLogger().addAppender(lf5);
												}
												lf5.getLogBrokerMonitor().show();
											}
										});
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
