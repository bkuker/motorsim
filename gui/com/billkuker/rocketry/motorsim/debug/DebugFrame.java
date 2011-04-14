package com.billkuker.rocketry.motorsim.debug;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.billkuker.rocketry.motorsim.visual.RememberJFrame;

public class DebugFrame extends RememberJFrame {
	private static final long serialVersionUID = 1L;

	public DebugFrame(){
		super(800,600);
		setTitle("MotorSim - Debug");
		JTabbedPane tabs = new JTabbedPane();
		this.setContentPane(tabs);
		tabs.add("Threads", new JScrollPane(new ThreadsPanel()));
		this.setVisible(true);
	}
}
