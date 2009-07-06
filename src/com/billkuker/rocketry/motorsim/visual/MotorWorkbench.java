package com.billkuker.rocketry.motorsim.visual;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

public class MotorWorkbench extends JFrame {
	private JPanel top;
	private JSplitPane split;
	private JTree tree;
	private JTabbedPane motors;
	private JToolBar bar;
	public MotorWorkbench(){
		setSize(1024,768);
		top = new JPanel( new BorderLayout());
		setContentPane(top);
		
		bar = new JToolBar();
		bar.add(new JButton("Burn"));
		top.add(bar, BorderLayout.PAGE_START);
		
		motors = new JTabbedPane();
		motors.addTab("Motor 1", new MotorEditor(MotorEditor.defaultMotor()));
		motors.addTab("Motor 2", new MotorEditor(MotorEditor.defaultMotor()));
		
		tree = new JTree();
		
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tree, motors);
		split.setDividerLocation(.25);
		split.setResizeWeight(.25);
		top.add(split, BorderLayout.CENTER);
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	public static void main(String args[]) throws Exception{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		new MotorWorkbench().show();
	}
}
