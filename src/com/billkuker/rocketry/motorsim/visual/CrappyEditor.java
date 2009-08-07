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
import javax.swing.UIManager;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.ConvergentDivergentNozzle;
import com.billkuker.rocketry.motorsim.CylindricalChamber;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.RocketScience.UnitPreference;
import com.billkuker.rocketry.motorsim.fuel.KNSU;
import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;
import com.billkuker.rocketry.motorsim.grain.MultiGrain;
import com.billkuker.rocketry.motorsim.io.MotorIO;
import com.billkuker.rocketry.motorsim.motors.example.CSlot;
import com.billkuker.rocketry.motorsim.motors.example.EndBurner;

public class CrappyEditor extends JFrame {
	private static final long serialVersionUID = 1L;
	JTabbedPane tabs;

	public CrappyEditor() {
		setTitle("MotorSim v0.3");
		setSize(1024, 768);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		tabs = new JTabbedPane();
		setContentPane(tabs);
		tabs.addTab("Edit", new Editor(defaultMotor()));

	}

	private class Editor extends JPanel {
		private static final long serialVersionUID = 1L;
		RSyntaxTextArea text = new RSyntaxTextArea();

		Editor(Motor m) {
			setLayout(new BorderLayout());
			add(text, BorderLayout.CENTER);
			JPanel buttons = new JPanel(new FlowLayout());

			buttons.add(new JButton("Burn!") {
				private static final long serialVersionUID = 1L;
				{
					addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							burn();
						}
					});
				}
			});


			JRadioButton s, n;
			buttons.add(s = new JRadioButton("SI"));
			buttons.add(n = new JRadioButton("NonSI"));
			ButtonGroup g = new ButtonGroup();
			g.add(s);
			g.add(n);
			s.setSelected(true);
			s.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					UnitPreference.preference = UnitPreference.SI;
				}
			});
			n.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					UnitPreference.preference = UnitPreference.NONSI;
				}
			});
			
			buttons.add(new JButton("End Burner Example") {
				private static final long serialVersionUID = 1L;
				{
					addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
								tabs.addTab(getText(), new BurnPanel(new Burn(new EndBurner())));
						}
					});
				}
			});
			
			buttons.add(new JButton("C-Slot Example") {
				private static final long serialVersionUID = 1L;
				{
					addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
								tabs.addTab(getText(), new BurnPanel(new Burn(new CSlot())));
						}
					});
				}
			});
			
			add(buttons, BorderLayout.SOUTH);
			text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);

			try {
				text.setText(MotorIO.writeMotor(m));
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void burn() {
			try {
				Motor m = MotorIO.readMotor(text.getText());
				Burn b = new Burn(m);
				tabs.addTab(m.getName() + " Output", new BurnPanel(b));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Motor defaultMotor() {
		Motor m = new Motor();
		m.setName("Example Motor");
		m.setFuel(new KNSU());

		CylindricalChamber c = new CylindricalChamber();
		c.setLength(Amount.valueOf(200, SI.MILLIMETER));
		c.setID(Amount.valueOf(25, SI.MILLIMETER));
		m.setChamber(c);

		CoredCylindricalGrain g = new CoredCylindricalGrain();
		try {
			g.setLength(Amount.valueOf(70, SI.MILLIMETER));
			g.setOD(Amount.valueOf(23.5, SI.MILLIMETER));
			g.setID(Amount.valueOf(7.9375, SI.MILLIMETER));
		} catch (PropertyVetoException v) {
			throw new Error(v);
		}

		m.setGrain(new MultiGrain(g, 2));


		ConvergentDivergentNozzle n = new ConvergentDivergentNozzle();
		n.setThroatDiameter(Amount.valueOf(7.962, SI.MILLIMETER));
		n.setExitDiameter(Amount.valueOf(13.79, SI.MILLIMETER));
		n.setEfficiency(.85);
		m.setNozzle(n);

		return m;
	}
	

	public static void main(String args[]) {
		new CrappyEditor().setVisible(true);
	}
}
