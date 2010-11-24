package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.io.ENGExporter;
import com.billkuker.rocketry.motorsim.io.MotorIO;
import com.billkuker.rocketry.motorsim.visual.MultiObjectEditor;
import com.billkuker.rocketry.motorsim.visual.RememberJFrame;

public class MotorsEditor extends MultiObjectEditor<Motor, MotorEditor> {
	private static final long serialVersionUID = 1L;

	MultiMotorThrustChart mbc = new MultiMotorThrustChart();
	MultiMotorPressureChart mpc = new MultiMotorPressureChart();
	JScrollPane mmtScroll;
	MultiMotorTable mmt = new MultiMotorTable();

	JFrame detached;
	JTabbedPane detachedTabs;

	public MotorsEditor(JFrame f) {
		super(f, "Motor");

		mmtScroll = new JScrollPane(mmt);
		


		addCreator(new ObjectCreator() {
			@Override
			public Motor newObject() {
				return MotorEditor.defaultMotor();
			}

			@Override
			public String getName() {
				return "Motor";
			}
		});

		detached = new RememberJFrame(800,600){
			private static final long serialVersionUID = 1L;
		};
		detached.setTitle(MotorWorkbench.name + " - All Motors");
		detached.setContentPane(detachedTabs = new JTabbedPane());

		detached.addWindowListener(new WindowListener() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				attach();
			}
			@Override
			public void windowOpened(WindowEvent arg0) {}
			@Override
			public void windowIconified(WindowEvent arg0) {}
			@Override
			public void windowDeiconified(WindowEvent arg0) {}
			@Override
			public void windowDeactivated(WindowEvent arg0) {}
			@Override
			public void windowClosed(WindowEvent arg0) {}
			@Override
			public void windowActivated(WindowEvent arg0) {}
		});
		attach();
	}

	public void attach() {
		detachedTabs.remove(mbc);
		detachedTabs.remove(mpc);
		detachedTabs.remove(mmtScroll);
		insertTab("All Motors", null, mmtScroll, null, 0);
		insertTab("All Thrust", null, mbc, null, 1);
		insertTab("All Pressure", null, mpc, null, 2);
		detached.setVisible(false);
	}
	
	public void detach() {
		if (detached.isVisible())
			return;
		remove(mbc);
		remove(mpc);
		remove(mmtScroll);
		detachedTabs.addTab("All Motors",mmtScroll);
		detachedTabs.addTab("All Thrust", mbc);
		detachedTabs.addTab("All Pressure", mpc);
		detached.setVisible(true);
	}

	@Override
	protected void objectAdded(Motor m, MotorEditor e) {
		e.addBurnWatcher(mbc);
		e.addBurnWatcher(mpc);
		e.addBurnWatcher(mmt);
	}

	@Override
	protected void objectRemoved(Motor m, MotorEditor e) {
		mbc.removeBurn(e.burn);
		mpc.removeBurn(e.burn);
		mmt.removeBurn(e.burn);
	}

	@Override
	public MotorEditor createEditor(Motor o) {
		return new MotorEditor(o);
	}

	@Override
	protected Motor loadFromFile(File f) throws IOException {
		return MotorIO.readMotor(new FileInputStream(f));
	}

	@Override
	protected void saveToFile(Motor o, File f) throws IOException {
		MotorIO.writeMotor(o, new FileOutputStream(f));
	}

	@Override
	public JMenu getMenu() {
		JMenu ret = super.getMenu();
		ret.add(new JSeparator());
		ret.add(new JMenuItem("Export .ENG") {
			private static final long serialVersionUID = 1L;
			{
				addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {

						final FileDialog fd = new FileDialog(frame,
								"Export .ENG File", FileDialog.SAVE);
						fd.setFile("motorsim.eng");
						fd.setVisible(true);
						if (fd.getFile() != null) {
							File file = new File(fd.getDirectory()
									+ fd.getFile());
							MotorEditor me = getSelectedEditor();
							Vector<Burn> bb = new Vector<Burn>();
							bb.add(me.burn);
							try {
								ENGExporter.export(bb, file);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				});
			}
		});
		return ret;
	}
}
