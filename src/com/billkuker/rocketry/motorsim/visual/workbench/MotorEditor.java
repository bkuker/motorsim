package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.Chamber;
import com.billkuker.rocketry.motorsim.ConvergentDivergentNozzle;
import com.billkuker.rocketry.motorsim.CylindricalChamber;
import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.MotorPart;
import com.billkuker.rocketry.motorsim.Nozzle;
import com.billkuker.rocketry.motorsim.fuel.EditableFuel;
import com.billkuker.rocketry.motorsim.fuel.KNDX;
import com.billkuker.rocketry.motorsim.fuel.KNER;
import com.billkuker.rocketry.motorsim.fuel.KNSB;
import com.billkuker.rocketry.motorsim.fuel.KNSU;
import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;
import com.billkuker.rocketry.motorsim.grain.Finocyl;
import com.billkuker.rocketry.motorsim.grain.Moonburner;
import com.billkuker.rocketry.motorsim.grain.MultiGrain;
import com.billkuker.rocketry.motorsim.grain.RodAndTubeGrain;
import com.billkuker.rocketry.motorsim.io.MotorIO;
import com.billkuker.rocketry.motorsim.visual.BurnPanel;
import com.billkuker.rocketry.motorsim.visual.Chart;
import com.billkuker.rocketry.motorsim.visual.Editor;
import com.billkuker.rocketry.motorsim.visual.GrainPanel;
import com.billkuker.rocketry.motorsim.visual.NozzlePanel;
import com.billkuker.rocketry.motorsim.visual.Chart.IntervalDomain;

public class MotorEditor extends JTabbedPane implements PropertyChangeListener,
		DocumentListener {
	private static final long serialVersionUID = 1L;
	RSyntaxTextArea text = new RSyntaxTextArea();
	Motor motor;
	GrainEditor grainEditor;
	BurnTab bt;

	private static final int XML_TAB = 0;
	private static final int CASING_TAB = 1;
	private static final int GRAIN_TAB = 2;
	private static final int FUEL_TAB = 3;
	private static final int BURN_TAB = 4;

	private Class[] grainTypes = { CoredCylindricalGrain.class, Finocyl.class,
			Moonburner.class, RodAndTubeGrain.class };

	private Class[] fuelTypes = { KNSB.class, KNSU.class, KNER.class,
			KNDX.class, EditableFuel.class };

	private abstract class Chooser<T> extends JPanel {
		private static final long serialVersionUID = 1L;
		private Class<? extends T>[] types;

		public Chooser(Class<? extends T>... ts) {
			types = ts;
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			for (final Class<? extends T> c : types) {
				JButton b = new JButton(c.getSimpleName());
				add(b);
				b.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							choiceMade(c.newInstance());
						} catch (InstantiationException e1) {
							e1.printStackTrace();
						} catch (IllegalAccessException e1) {
							e1.printStackTrace();
						}
					}
				});
			}
		}

		protected abstract void choiceMade(T o);
	}

	private class BurnTab extends JPanel {
		public BurnTab() {
			setLayout(new BorderLayout());
			setName("Burn");
			reBurn();
		}

		public void reBurn() {
			removeAll();
			new Thread() {
				public void run() {
					final Burn b = new Burn(motor);
					final BurnPanel bp = new BurnPanel(b);
					SwingUtilities.invokeLater(new Thread() {
						public void run() {
							add(bp, BorderLayout.CENTER);
						}
					});
				}
			}.start();
		}
	}

	private class GrainEditor extends JSplitPane {
		private static final long serialVersionUID = 1L;

		public GrainEditor(final Grain g) {
			super(JSplitPane.HORIZONTAL_SPLIT);
			setName("Grain");
			setRightComponent(new GrainPanel(g));
			if (g instanceof Grain.Composite) {
				final JPanel p = new JPanel();
				p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
				p.add(new Editor(g));
				for (Grain gg : ((Grain.Composite) g).getGrains()) {
					final int grainEditorIndex = p.getComponentCount() + 1;
					p.add(new Chooser<Grain>(grainTypes) {
						private static final long serialVersionUID = 1L;

						@Override
						protected void choiceMade(Grain ng) {
							if (g instanceof MultiGrain) {
								((MultiGrain) g).setGrain(ng);
								p.remove(grainEditorIndex);
								p.add(new Editor(ng), grainEditorIndex);
								p.remove(0);
								p.add(new Editor(g), 0);
								// System.out.println("Chose new grain");
							}
						}
					});
					p.add(new Editor(gg));
					if (gg instanceof MotorPart) {
						((MotorPart) gg)
								.addPropertyChangeListener(MotorEditor.this);
					}
				}
				setLeftComponent(p);
			} else {
				setLeftComponent(new Editor(g));
			}
			// setDividerLocation(.25);
			// setResizeWeight(.25);
			if (g instanceof MotorPart) {
				((MotorPart) g).addPropertyChangeListener(MotorEditor.this);
			}
		}
	}

	private class FuelEditor extends JSplitPane {
		private static final long serialVersionUID = 1L;

		public FuelEditor(Fuel f) {
			super(JSplitPane.HORIZONTAL_SPLIT);
			setName("Fuel");
			Chart<Pressure, Velocity> burnRate;
			try {
				burnRate = new Chart<Pressure, Velocity>(SI.MEGA(SI.PASCAL),
						SI.METERS_PER_SECOND, f, "burnRate");
			} catch (NoSuchMethodException e) {
				throw new Error(e);
			}
			burnRate.setDomain(burnRate.new IntervalDomain(Amount.valueOf(0, SI
					.MEGA(SI.PASCAL)), Amount.valueOf(11, SI.MEGA(SI.PASCAL)),
					20));

			final JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

			p.add(new Chooser<Fuel>(fuelTypes) {
				@Override
				protected void choiceMade(Fuel o) {
					motor.setFuel(o);
					removeTabAt(FUEL_TAB);
					MotorEditor.this.add(new FuelEditor(motor.getFuel()),
							FUEL_TAB);
					setSelectedIndex(FUEL_TAB);
				}
			});
			p.add(new Editor(f));
			try {
				p.add(new Editor(f.getCombustionProduct()));
			} catch (Exception e) {

			}

			setLeftComponent(p);
			setRightComponent(burnRate);
			// setDividerLocation(.25);
			// setResizeWeight(.25);
			if (f instanceof MotorPart) {
				((MotorPart) f).addPropertyChangeListener(MotorEditor.this);
			}
		}
	}

	private class CaseEditor extends JSplitPane {
		private static final long serialVersionUID = 1L;

		public CaseEditor(Nozzle n, Chamber c) {
			super(JSplitPane.HORIZONTAL_SPLIT);
			setName("Casing");
			JPanel parts = new JPanel();
			parts.setLayout(new BoxLayout(parts, BoxLayout.Y_AXIS));
			setLeftComponent(parts);
			setRightComponent(new NozzlePanel(n));

			parts.add(new Editor(c));
			parts.add(new Editor(n));

			if (n instanceof MotorPart) {
				((MotorPart) n).addPropertyChangeListener(MotorEditor.this);
			}
			if (c instanceof MotorPart) {
				((MotorPart) c).addPropertyChangeListener(MotorEditor.this);
			}
		}
	}

	{
		text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);

	}

	public MotorEditor(Motor m) {
		super(JTabbedPane.BOTTOM);
		text.getDocument().addDocumentListener(this);
		text.setName("XML");
		add(text, XML_TAB);
		setMotor(m, true);
	}

	private void reText() {
		try {
			text.setText(MotorIO.writeMotor(motor));
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	private void setMotor(Motor m, boolean retext) {
		motor = m;
		if (retext)
			reText();
		if (grainEditor != null)
			remove(grainEditor);
		while (getTabCount() > 1)
			removeTabAt(1);
		add(new CaseEditor(motor.getNozzle(), motor.getChamber()), CASING_TAB);
		add(new GrainEditor(motor.getGrain()), GRAIN_TAB);
		add(new FuelEditor(motor.getFuel()), FUEL_TAB);
		add(bt = new BurnTab(), BURN_TAB);
	}

	@Deprecated
	public static Motor defaultMotor() {
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

	public void focusOnObject(Object o) {
		if (o instanceof Grain)
			setSelectedIndex(GRAIN_TAB);
		if (o instanceof Chamber || o instanceof Nozzle)
			setSelectedIndex(CASING_TAB);
		if (o instanceof Fuel || o instanceof Fuel.CombustionProduct)
			setSelectedIndex(FUEL_TAB);
	}

	@Deprecated
	public void showAsWindow() {
		JFrame f = new JFrame();
		f.setSize(1024, 768);
		f.setContentPane(this);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setVisible(true);
	}

	public static void main(String args[]) throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		new MotorEditor(defaultMotor()).showAsWindow();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		reText();
		bt.reBurn();
	}

	public void changedUpdate(DocumentEvent e) {
		try {
			final Motor m = MotorIO.readMotor(text.getText());
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setMotor(m, false);
				}
			});
			System.out.println("Motor Updated");
		} catch (Throwable e1) {
			// Leave blank, motor might be broken
		}
	}

	public void insertUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
	}

	public void removeUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
	}
}
