package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.RocketScience.UnitPreference;
import com.billkuker.rocketry.motorsim.fuel.FuelResolver;
import com.billkuker.rocketry.motorsim.io.ENGExporter;
import com.billkuker.rocketry.motorsim.io.MotorIO;

import fuel.FuelsEditor;

public class MotorWorkbench extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JPanel top;
	private JTabbedPane motors;
	private MultiBurnChart mb;
	private JFrame allBurns;
	
	private JFrame fuelEditorFrame = new JFrame(){
		private static final long serialVersionUID = 1L;
		{
			setSize(1024, 768);
			add(fuelEditor = new FuelsEditor(this));
			JMenuBar b;
			setJMenuBar(b = new JMenuBar());
			b.add(fuelEditor.getMenu());
			setTitle("MotorSim - Fuel Editor");
		}
	};
	private FuelsEditor fuelEditor;

	private HashMap<MotorEditor, File> e2f = new HashMap<MotorEditor, File>();
	private HashMap<File, MotorEditor> f2e = new HashMap<File, MotorEditor>();

	private HashMap<Motor, MotorEditor> m2e = new HashMap<Motor, MotorEditor>();
	
	public MotorWorkbench() {
		setTitle("MotorSim 1.0 RC1");
		addMenu();
		setSize(1024, 768);
		top = new JPanel(new BorderLayout());
		setContentPane(top);
		
		mb = new MultiBurnChart();
		allBurns = new JFrame();
		allBurns.setTitle("All Burns");
		allBurns.setSize(800, 600);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		allBurns.add(mb);

		motors = new JTabbedPane();
		
		top.add(motors, BorderLayout.CENTER);
		
		for ( Fuel f : FuelResolver.getFuelMap().values() ){
			addFuel(f);
		}

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setVisible(true);

	}

	private void addMenu() {

		setJMenuBar(new JMenuBar() {
			private static final long serialVersionUID = 1L;

			{
				add(new JMenu("File") {
					private static final long serialVersionUID = 1L;

					{
						add(new JMenuItem("New Motor") {
							private static final long serialVersionUID = 1L;
							{
								addActionListener(new ActionListener() {

									@Override
									public void actionPerformed(ActionEvent arg0) {
										addMotor(MotorEditor.defaultMotor(), null);
									}
								});

							}
						});
						add(new JMenuItem("Open...") {
							private static final long serialVersionUID = 1L;
							{
								addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent arg0) {

										final FileDialog fd = new FileDialog(MotorWorkbench.this, "Open Motor", FileDialog.LOAD);
										fd.setVisible(true);
										if ( fd.getFile() != null ) {
											File file = new File(fd.getDirectory() + fd.getFile());
											if (f2e.get(file) != null) {
												motors.setSelectedComponent(f2e
														.get(file));
												return;
											}
											try {
												Motor m = MotorIO
														.readMotor(new FileInputStream(file));
												addMotor(m, file);
												
											} catch (Exception e) {
												JOptionPane.showMessageDialog(
														MotorWorkbench.this, e
																.getMessage());
											}
										}
									}
								});
							}
						});
						
						add(new JSeparator());
						
						add(new JMenuItem("Close Motor") {
							private static final long serialVersionUID = 1L;
							{
								addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent ev) {
										MotorEditor e = (MotorEditor) motors
												.getSelectedComponent();
										motors.remove(e);
										f2e.remove(e2f.get(e));
										e2f.remove(e);
										m2e.remove(e.getMotor());
										mb.removeBurn(e.burn);
									}
								});
							}
						});
						
						add(new JMenuItem("Save Motor") {
							private static final long serialVersionUID = 1L;
							{
								addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										MotorEditor m = (MotorEditor) motors
												.getSelectedComponent();
										File f = e2f.get(m);
										if (f != null)
											save(m.getMotor(), f);

									}
								});
							}
						});
						add(new JMenuItem("Save Motor As...") {
							private static final long serialVersionUID = 1L;
							{
								addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent arg0) {

										final FileDialog fd = new FileDialog(MotorWorkbench.this, "Save Motor As", FileDialog.SAVE);
										fd.setVisible(true);
										if (fd.getFile() != null ) {
											File file = new File(fd.getDirectory() + fd.getFile());
											MotorEditor m = (MotorEditor) motors
													.getSelectedComponent();
											try {
												save(m.getMotor(), file);
												e2f.put(m, file);
												f2e.put(file, m);
												motors.setTitleAt(motors
														.getSelectedIndex(),
														file.getName());
												// TODO Set tab title
											} catch (Exception e) {
												JOptionPane.showMessageDialog(
														MotorWorkbench.this, e
																.getMessage());
											}
										}
									}
								});
							}
						});
						

						add(new JSeparator());
						add(new JMenuItem("Export .ENG"){
							private static final long serialVersionUID = 1L;

							{
								addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent arg0) {

										final FileDialog fd = new FileDialog(MotorWorkbench.this, "Export .ENG File", FileDialog.SAVE);
										fd.setFile("motorsim.eng");
										fd.setVisible(true);
										if (fd.getFile() != null ) {
											File file = new File(fd.getDirectory() + fd.getFile());
											Vector<Burn> bb = new Vector<Burn>();
											for( MotorEditor me : m2e.values() )
												bb.add(me.burn);
											try{
												ENGExporter.export(bb, file);
											} catch ( Exception e ){
												e.printStackTrace();
											}
										}
									}
								});
							}
						});
					}
				});
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
						add(new JMenuItem("All Motors Graph") {
							private static final long serialVersionUID = 1L;
							{
								addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent arg0) {
										allBurns.setVisible(true);
										allBurns.toFront();
									}
								});
							}
						});
						add(new JMenuItem("Fuel Editor") {
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
	
	private void addFuel(Fuel f){
		for ( MotorEditor e : m2e.values() )
			e.addFuel(f);
	}
	

	private void save(Motor m, File f) {
		try {
			FileOutputStream fo;
			MotorIO.writeMotor(m, fo = new FileOutputStream(f));
			fo.close();
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(MotorWorkbench.this, t.getMessage());
		}
	}

	public void addMotor(Motor m, File f) {
		MotorEditor e = new MotorEditor(m, FuelResolver.getFuelMap().values());
		e.addBurnWatcher(mb);
		String title;
		if (f == null) {
			title = "New Motor";
		} else {
			title = f.getName();
			e2f.put(e, f);
			f2e.put(f, e);
		}
		m2e.put(m, e);
		motors.addTab(title, e);
	}

	
}
