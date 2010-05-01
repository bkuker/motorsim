package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.RocketScience.UnitPreference;
import com.billkuker.rocketry.motorsim.io.ENGExporter;
import com.billkuker.rocketry.motorsim.io.MotorIO;
import com.billkuker.rocketry.motorsim.visual.workbench.WorkbenchTreeModel.FuelEditNode;

public class MotorWorkbench extends JFrame implements TreeSelectionListener {
	private static final long serialVersionUID = 1L;
	private JPanel top;
	private JSplitPane split;
	private JTree tree;
	private JTabbedPane motors;
	private WorkbenchTreeModel tm;
	private MultiBurnChart mb;
	private JFrame allBurns;

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
		allBurns.setVisible(true);
		
		motors = new JTabbedPane();

		tree = new JTree(tm = new WorkbenchTreeModel());
		tree.setCellRenderer(new WorkbenchTreeCellRenderer());
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setPreferredSize(new Dimension(200, 100));

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(
				tree), motors);
		// split.setDividerLocation(.25);
		// split.setResizeWeight(.25);
		top.add(split, BorderLayout.CENTER);

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
										addMotor(MotorEditor.defaultMotor(),
												null);
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
														.readMotor(file);
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
										tm.removeMotor(e.getMotor());
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
						add(new JMenuItem("New Fuel") {
							private static final long serialVersionUID = 1L;
							{
								addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent arg0) {
										addFuel();
									}
								});

							}
						});
						add(new JMenuItem("Save Fuel") {});
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
						sci.setSelected(true);
						sci.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								UnitPreference.preference = UnitPreference.SI;
							}
						});
						nonsci.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								UnitPreference.preference = UnitPreference.NONSI;
							}
						});
						add(sci);
						add(nonsci);
					}
				});
			}
		});
	}
	
	private void addFuel(){
		final SRFuelEditor ed = new SRFuelEditor();
		final FuelEditNode node = tm.new FuelEditNode(ed);
		tm.getFuels().add(node);
		tm.nodeStructureChanged(tm.getFuels());
		motors.addTab(ed.getFuel().getName(), ed);
		ed.getFuel().addPropertyChangeListener(new PropertyChangeListener(){
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ( evt.getPropertyName().equals("Name")){
					for ( int i = 0; i < motors.getTabCount(); i++ ){
						if ( motors.getComponent(i) == ed ){
							motors.setTitleAt(i, ed.getFuel().getName());
							tm.nodeChanged(node);
						}
					}
				}
			}});
	}

	private void save(Motor m, File f) {
		try {
			MotorIO.writeMotor(m, f);
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(MotorWorkbench.this, t.getMessage());
		}
	}

	public void addMotor(Motor m, File f) {
		tm.addMotor(m);
		MotorEditor e = new MotorEditor(m);
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

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		if ( e.getPath().getLastPathComponent() == tm.getMotors() ){
			allBurns.setVisible(true);
			allBurns.toFront();
		}
		
		if ( e.getPath().getLastPathComponent() instanceof FuelEditNode ){
			FuelEditNode fen = ((FuelEditNode)e.getPath().getLastPathComponent());
			SRFuelEditor ed = fen.getUserObject();
			for ( int i = 0 ; i < motors.getTabCount(); i++ ){
				motors.setSelectedComponent(ed);
			}
		}
		
		Motor m = getMotor(e.getPath());
		
		if ( m == null )
			return;

		motors.setSelectedComponent(m2e.get(m));

		if (e.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
			Object o = ((DefaultMutableTreeNode) e.getPath()
					.getLastPathComponent()).getUserObject();
			m2e.get(m).focusOnObject(o);
		}
		

	}

	private Motor getMotor(TreePath p) {
		if (p.getLastPathComponent() instanceof WorkbenchTreeModel.MotorNode) {
			return ((WorkbenchTreeModel.MotorNode) p.getLastPathComponent())
					.getUserObject();
		} else if (p.getPath().length > 1)
			return getMotor(p.getParentPath());
		return null;
	}
}
