package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.RocketScience.UnitPreference;
import com.billkuker.rocketry.motorsim.fuel.FuelResolver;
import com.billkuker.rocketry.motorsim.fuel.PiecewiseLinearFuel;
import com.billkuker.rocketry.motorsim.fuel.SaintRobertFuel;
import com.billkuker.rocketry.motorsim.io.ENGExporter;
import com.billkuker.rocketry.motorsim.io.MotorIO;
import com.billkuker.rocketry.motorsim.visual.FuelPanel;
import com.billkuker.rocketry.motorsim.visual.workbench.AbstractFuelEditor.EditablePSRFuel;
import com.billkuker.rocketry.motorsim.visual.workbench.WorkbenchTreeModel.FuelEditNode;
import com.billkuker.rocketry.motorsim.visual.workbench.WorkbenchTreeModel.FuelNode;

public class MotorWorkbench extends JFrame implements TreeSelectionListener {
	private static final long serialVersionUID = 1L;
	
	private JPanel top;
	private JSplitPane split;
	private JTree tree;
	private JTabbedPane motors;
	private JTabbedPane fuels;
	private WorkbenchTreeModel tm;
	private MultiBurnChart mb;
	private JFrame allBurns;

	private HashMap<MotorEditor, File> e2f = new HashMap<MotorEditor, File>();
	private HashMap<File, MotorEditor> f2e = new HashMap<File, MotorEditor>();

	private HashMap<Motor, MotorEditor> m2e = new HashMap<Motor, MotorEditor>();

	private static final int TREE_WIDTH = 200;
	
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
		fuels = new JTabbedPane();

		tree = new JTree(tm = new WorkbenchTreeModel());
		tree.setCellRenderer(new WorkbenchTreeCellRenderer());
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setMinimumSize(new Dimension(TREE_WIDTH, 100));

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
				tree, motors);
		split.setDividerLocation(TREE_WIDTH);
		split.revalidate();
		
		top.add(split, BorderLayout.CENTER);
		
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
						add(new JMenu("New Fuel"){
							private static final long serialVersionUID = 1L;
							{
								add(new JMenuItem("Saint-Robert") {
									private static final long serialVersionUID = 1L;
									{
										addActionListener(new ActionListener() {
											@Override
											public void actionPerformed(ActionEvent arg0) {
												newFuel(new SRFuelEditor(new EditablePSRFuel(SaintRobertFuel.Type.SI)));
											}
										});

									}
								});
								add(new JMenuItem("Linear"){
									private static final long serialVersionUID = 1L;
									{
										addActionListener(new ActionListener() {
											@Override
											public void actionPerformed(ActionEvent arg0) {
												newFuel(new LinearFuelEditor(new PiecewiseLinearFuel()));
											}
										});

									}
								});
							}
						});

						add(new JMenuItem("Save Fuel") {
							private static final long serialVersionUID = 1L;
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
						add(new JMenuItem("Show All Motors Graph") {
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
					}
				});
			}
		});
	}
	
	private void addFuel(Fuel f){
		for ( MotorEditor e : m2e.values() )
			e.addFuel(f);
		FuelPanel fp = new FuelPanel(f);
		FuelNode fn = tm.new FuelNode(fp, f);
		tm.getFuels().add(fn);
		tm.nodeStructureChanged(tm.getFuels());
		fuels.addTab(f.getName(), fp);
	}
	
	private void newFuel(final AbstractFuelEditor ed){
		for ( MotorEditor e : m2e.values() )
			e.addFuel(ed.getFuel());
		final FuelEditNode node = tm.new FuelEditNode(ed);
		tm.getFuels().add(node);
		tm.nodeStructureChanged(tm.getFuels());
		fuels.addTab(ed.getFuel().getName(), ed);
		ed.getFuel().addPropertyChangeListener(new PropertyChangeListener(){
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ( evt.getPropertyName().equals("Name")){
					for ( int i = 0; i < fuels.getTabCount(); i++ ){
						if ( fuels.getComponent(i) == ed ){
							fuels.setTitleAt(i, ed.getFuel().getName());
							tm.nodeChanged(node);
						}
					}
				}
			}});
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
		tm.addMotor(m);
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

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		if (e.getPath().getLastPathComponent() instanceof FuelNode) {
			FuelNode fen = ((FuelNode) e.getPath().getLastPathComponent());
			fuels.setSelectedComponent(fen.getUserObject());
			split.setRightComponent(fuels);
			split.setDividerLocation(TREE_WIDTH);
			split.revalidate();
		}

		Motor m = getMotor(e.getPath());

		if (m == null)
			return;

		split.setRightComponent(motors);
		split.setDividerLocation(TREE_WIDTH);
		split.revalidate();
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
