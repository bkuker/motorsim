package com.billkuker.rocketry.motorsim.visual;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import com.billkuker.rocketry.motorsim.visual.workbench.WorkbenchTreeCellRenderer;
import com.billkuker.rocketry.motorsim.visual.workbench.WorkbenchTreeModel;

public class MotorWorkbench extends JFrame implements TreeSelectionListener {
	private static final long serialVersionUID = 1L;
	private JPanel top;
	private JSplitPane split;
	private JTree tree;
	private JTabbedPane motors;
	private JToolBar bar;
	private WorkbenchTreeModel tm;

	private HashMap<Motor, MotorEditor> m2e = new HashMap<Motor, MotorEditor>();

	public MotorWorkbench() {
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
		//split.setDividerLocation(.25);
		//split.setResizeWeight(.25);
		top.add(split, BorderLayout.CENTER);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);

		Motor mot = MotorEditor.defaultMotor();
		addMotor(mot);
		//mot = MotorEditor.defaultMotor();
		//addMotor(mot);
	}

	public void addMotor(Motor m) {
		tm.addMotor(m);
		MotorEditor e = new MotorEditor(m);
		m2e.put(m, e);
		motors.addTab("Motor", e);
	}

	public static void main(String args[]) throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		new MotorWorkbench().show();
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		Motor m = getMotor(e.getPath());

		motors.setSelectedComponent(m2e.get(m));
		
		if ( e.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode ){
			Object o = ((DefaultMutableTreeNode)e.getPath().getLastPathComponent()).getUserObject();
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
