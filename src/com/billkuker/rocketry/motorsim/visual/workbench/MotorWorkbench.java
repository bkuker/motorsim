package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
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
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.RocketScience.UnitPreference;
import com.billkuker.rocketry.motorsim.io.MotorIO;

public class MotorWorkbench extends JFrame implements TreeSelectionListener {
	private static final long serialVersionUID = 1L;
	private JPanel top;
	private JSplitPane split;
	private JTree tree;
	private JTabbedPane motors;
	private JToolBar bar;
	private WorkbenchTreeModel tm;

	private HashMap<MotorEditor, File> e2f = new HashMap<MotorEditor, File>();
	private HashMap<File, MotorEditor> f2e = new HashMap<File, MotorEditor>();

	private HashMap<Motor, MotorEditor> m2e = new HashMap<Motor, MotorEditor>();

	public MotorWorkbench() {
		setTitle("MotorWorkbench");
		addMenu();
		setSize(1024, 768);
		top = new JPanel(new BorderLayout());
		setContentPane(top);

		bar = new JToolBar();
		bar.add(new JButton("Burn"));
		top.add(bar, BorderLayout.PAGE_START);

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

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);

	}

	private void addMenu() {

		setJMenuBar(new JMenuBar() {
			private static final long serialVersionUID = 1L;

			{
				add(new JMenu("File") {
					private static final long serialVersionUID = 1L;

					{
						add(new JMenuItem("New") {
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

										final JFileChooser fc = new JFileChooser();
										int returnVal = fc
												.showOpenDialog(MotorWorkbench.this);
										if (returnVal == JFileChooser.APPROVE_OPTION) {
											File file = fc.getSelectedFile();
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
						add(new JMenuItem("Close") {
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
									}
								});
							}
						});
						add(new JSeparator());
						add(new JMenuItem("Save") {
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
						add(new JMenuItem("Save As...") {
							private static final long serialVersionUID = 1L;
							{
								addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent arg0) {

										final JFileChooser fc = new JFileChooser();
										int returnVal = fc
												.showSaveDialog(MotorWorkbench.this);
										if (returnVal == JFileChooser.APPROVE_OPTION) {
											File file = fc.getSelectedFile();
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
		new MotorWorkbench().setVisible(true);
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		Motor m = getMotor(e.getPath());

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
