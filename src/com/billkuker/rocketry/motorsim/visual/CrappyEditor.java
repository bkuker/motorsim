package com.billkuker.rocketry.motorsim.visual;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.IOException;

import javax.measure.unit.SI;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.ConvergentDivergentNozzle;
import com.billkuker.rocketry.motorsim.CylindricalChamber;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.RocketScience;
import com.billkuker.rocketry.motorsim.RocketScience.UnitPreference;
import com.billkuker.rocketry.motorsim.fuel.KNSU;
import com.billkuker.rocketry.motorsim.fuel.EditableFuel;
import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;
import com.billkuker.rocketry.motorsim.grain.MultiGrain;
import com.billkuker.rocketry.motorsim.io.MotorIO;
import com.billkuker.rocketry.motorsim.motors.kuker.PVC9;

public class CrappyEditor extends JFrame {
	JTabbedPane tabs;
	JPanel editor;
	//JTextArea text = new JTextArea();
	RSyntaxTextArea text;
	public CrappyEditor(){
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		tabs = new JTabbedPane();
		editor = new JPanel();
		text = new RSyntaxTextArea();
		
		setTitle("MotorSim v0.1");
		setSize(1024, 768);
		
		setContentPane(tabs);
		
		text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		
		try {
			text.setText(MotorIO.writeMotor(defaultMotor()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		editor.setLayout(new BorderLayout());
		editor.add(text, BorderLayout.CENTER);
		JPanel buttons = new JPanel(new FlowLayout());
		
		buttons.add(new JButton("Burn!"){
			{
				addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent arg0) {
						burn();
					}
				});
			}
		});
		
		{
			JRadioButton s, n;
			buttons.add(s = new JRadioButton("SI"));
			buttons.add(n = new JRadioButton("NonSI"));
			ButtonGroup g = new ButtonGroup();
			g.add(s);
			g.add(n);
			s.setSelected(true);
			s.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					UnitPreference.preference = UnitPreference.SI;	
				}
			});
			n.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					UnitPreference.preference = UnitPreference.NONSI;	
				}
			});
		}
		
		editor.add(buttons, BorderLayout.SOUTH);
		
		tabs.addTab("Edit", editor);
		
		
	}
	
	private Motor defaultMotor(){
		Motor m = new Motor();
		m.setName("PVC9");
		m.setFuel(new KNSU());

		CylindricalChamber c = new CylindricalChamber();
		c.setLength(Amount.valueOf(200, SI.MILLIMETER));
		c.setID(Amount.valueOf(30, SI.MILLIMETER));
		m.setChamber(c);

		CoredCylindricalGrain g = new CoredCylindricalGrain();
		try {
			g.setLength(Amount.valueOf(70, SI.MILLIMETER));
			g.setOD(Amount.valueOf(29, SI.MILLIMETER));
			g.setID(Amount.valueOf(8, SI.MILLIMETER));
		} catch (PropertyVetoException v) {
			throw new Error(v);
		}

		m.setGrain(new MultiGrain(g, 2));

		ConvergentDivergentNozzle n = new ConvergentDivergentNozzle();
		n.setThroatDiameter(Amount.valueOf(7.9, SI.MILLIMETER));
		n.setExitDiameter(Amount.valueOf(9, SI.MILLIMETER));
		n.setEfficiency(.87);
		m.setNozzle(n);
		
		return m;
	}
	
	private void burn(){
		try {
			Motor m = MotorIO.readMotor(text.getText());
			Burn b = new Burn(m);
			tabs.addTab(m.getName() + " Output", new BurnPanel(b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]){
		new CrappyEditor().show();
	}
}
