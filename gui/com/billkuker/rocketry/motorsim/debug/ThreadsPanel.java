package com.billkuker.rocketry.motorsim.debug;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class ThreadsPanel extends JTable {
	private static final long serialVersionUID = 1L;

	private DefaultTableModel tm = new DefaultTableModel();
	static ThreadMXBean tmbean = ManagementFactory.getThreadMXBean();

	public ThreadsPanel() {
		tm.addColumn("Name");
		tm.addColumn("ID");
		tm.addColumn("CPU");
		setModel(tm);

		new Thread() {
			{
				setDaemon(true);
				setName("Debug - Thread List");
			}
			@Override
			public void run() {
				while (true) {
					try {
						SwingUtilities.invokeLater(new Thread() {
							@Override
							public void run() {
								try {
									while (tm.getRowCount() > 0)
										tm.removeRow(0);

									long[] tids = tmbean.getAllThreadIds();
									ThreadInfo[] tinfos = tmbean.getThreadInfo(
											tids, Integer.MAX_VALUE);
									for (ThreadInfo ti : tinfos) {
										tm.addRow(new Object[] {
												ti.getThreadName(),
												ti.getThreadId(),
												tmbean.getThreadCpuTime(ti
														.getThreadId()),
														});
									}
								} catch (Exception e) {
									e.printStackTrace();
								}

							}
						});
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

}
