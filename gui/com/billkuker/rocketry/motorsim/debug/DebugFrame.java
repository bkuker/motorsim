package com.billkuker.rocketry.motorsim.debug;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class DebugFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	public DebugFrame(){
		setSize(800,600);
		setTitle("MotorSim - Debug");
		JTabbedPane tabs = new JTabbedPane();
		this.setContentPane(tabs);
		tabs.add("Threads", new JScrollPane(new ThreadsPanel()));
		this.setVisible(true);
	}
}
