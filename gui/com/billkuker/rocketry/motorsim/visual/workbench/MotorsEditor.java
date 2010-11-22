package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import java.awt.event.ActionListener;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.io.ENGExporter;
import com.billkuker.rocketry.motorsim.io.MotorIO;
import com.billkuker.rocketry.motorsim.visual.MultiObjectEditor;

public class MotorsEditor extends MultiObjectEditor<Motor, MotorEditor> {
	private static final long serialVersionUID = 1L;
	
	MultiBurnChart mbc = new MultiBurnChart();

	public MotorsEditor(JFrame f) {
		super(f, "Motor");
		
		addTab("Thrust Graphs", mbc);
		
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
	}
	
	@Override
	protected void objectAdded(Motor m, MotorEditor e){
		e.addBurnWatcher(mbc);
	}
	
	@Override
	protected void objectRemoved(Motor m, MotorEditor e){
		mbc.removeBurn(e.burn);
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
	public JMenu getMenu(){
		JMenu ret = super.getMenu();
		ret.add(new JSeparator());
		ret.add(new JMenuItem("Export .ENG"){
			private static final long serialVersionUID = 1L;
			{
				addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {

						final FileDialog fd = new FileDialog(frame, "Export .ENG File", FileDialog.SAVE);
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
