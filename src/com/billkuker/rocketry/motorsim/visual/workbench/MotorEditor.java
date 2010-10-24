package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.Chamber;
import com.billkuker.rocketry.motorsim.ChangeListening;
import com.billkuker.rocketry.motorsim.ConvergentDivergentNozzle;
import com.billkuker.rocketry.motorsim.CylindricalChamber;
import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.Nozzle;
import com.billkuker.rocketry.motorsim.fuel.EditableFuel;
import com.billkuker.rocketry.motorsim.fuel.KNDX;
import com.billkuker.rocketry.motorsim.fuel.KNER;
import com.billkuker.rocketry.motorsim.fuel.KNSB;
import com.billkuker.rocketry.motorsim.fuel.KNSU;
import com.billkuker.rocketry.motorsim.grain.CSlot;
import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;
import com.billkuker.rocketry.motorsim.grain.EndBurner;
import com.billkuker.rocketry.motorsim.grain.Finocyl;
import com.billkuker.rocketry.motorsim.grain.Moonburner;
import com.billkuker.rocketry.motorsim.grain.MultiGrain;
import com.billkuker.rocketry.motorsim.grain.RodAndTubeGrain;
import com.billkuker.rocketry.motorsim.io.MotorIO;
import com.billkuker.rocketry.motorsim.visual.BurnPanel;
import com.billkuker.rocketry.motorsim.visual.Chart;
import com.billkuker.rocketry.motorsim.visual.Editor;
import com.billkuker.rocketry.motorsim.visual.GrainPanel;
import com.billkuker.rocketry.motorsim.visual.HardwarePanel;

public class MotorEditor extends JTabbedPane implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(MotorEditor.class);
	RSyntaxTextArea text = new RSyntaxTextArea();
	Motor motor;
	GrainEditor grainEditor;
	BurnTab bt;
	Burn burn;

	private Vector<BurnWatcher> burnWatchers = new Vector<BurnWatcher>();
	private ComboBoxModel availableFuels;

	//private static final int XML_TAB = 0;
	private static final int CASING_TAB = 0;
	private static final int GRAIN_TAB = 1;
	private static final int BURN_TAB = 2;

	@SuppressWarnings("unchecked")
	private Class[] grainTypes = { CoredCylindricalGrain.class, Finocyl.class,
			Moonburner.class, RodAndTubeGrain.class, CSlot.class, EndBurner.class };

	private abstract class Chooser<T> extends JPanel {
		private static final long serialVersionUID = 1L;
		private Class<? extends T>[] types;
		private Map<Class<? extends T>, T> old = new HashMap<Class<? extends T>, T>();

		public Chooser(T initial, Class<? extends T>... ts) {
			types = ts;
			if ( initial != null )
				old.put((Class<? extends T>)initial.getClass(), initial);
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			for (final Class<? extends T> c : types) {
				JButton b = new JButton(c.getSimpleName());
				add(b);
				b.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							T val = old.get(c);
							if ( val == null ){
								System.err.println("CREATED NEW =========================");
								val = c.newInstance();
								old.put(c, val);
							}
							choiceMade(val);
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
		private static final long serialVersionUID = 1L;
		private Thread currentThread;
		
		public BurnTab() {
			setLayout(new BorderLayout());
			setName("Simulation Results");
			reBurn();
		}
		
		private class BurnCanceled extends RuntimeException{
			private static final long serialVersionUID = 1L;
		};

		public void reBurn() {
			removeAll();
			currentThread = new Thread() {
				public void run() {
					final Thread me = this;
					final JProgressBar bar = new JProgressBar(0, 100);
					add(bar, BorderLayout.NORTH);
					try {
						final Burn b = new Burn(motor,
								new Burn.BurnProgressListener() {
									@Override
									public void setProgress(float f) {
										bar.setValue((int) (f * 100));
										if ( currentThread != me ){
											throw new BurnCanceled();
										}
									}
								});

						final BurnPanel bp = new BurnPanel(b);
						SwingUtilities.invokeLater(new Thread() {
							public void run() {
								remove(bar);
								add(bp, BorderLayout.CENTER);

								for (BurnWatcher bw : burnWatchers)
									bw.replace(burn, b);
								burn = b;

								revalidate();
							}
						});
					} catch (BurnCanceled c){
						log.info("Burn Canceled!");
					} catch (Exception e) {
						remove(bar);
						JTextArea t = new JTextArea(e.getMessage());
						t.setEditable(false);
						add(t);
					}
				}
			};
			currentThread.start();
		}
	}

	private class GrainEditor extends JSplitPane {
		private static final long serialVersionUID = 1L;

		public GrainEditor(final Grain g) {
			super(JSplitPane.HORIZONTAL_SPLIT);
			setName("Grain Geometry");
			setRightComponent(new GrainPanel(g));
			if (g instanceof Grain.Composite) {
				final JPanel p = new JPanel();
				p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
				p.add(new Editor(g));
				for (Grain gg : ((Grain.Composite) g).getGrains()) {
					final int grainEditorIndex = p.getComponentCount() + 1;
					p.add(new Chooser<Grain>(gg, grainTypes) {
						private static final long serialVersionUID = 1L;

						@Override
						protected void choiceMade(Grain ng) {
							if (g instanceof MultiGrain) {
								((MultiGrain) g).setGrain(ng);
								p.remove(grainEditorIndex);
								p.add(new Editor(ng), grainEditorIndex);
								p.remove(0);
								p.add(new Editor(g), 0);
							}
						}
					});
					p.add(new Editor(gg));
					if (gg instanceof ChangeListening.Subject) {
						((ChangeListening.Subject) gg)
								.addPropertyChangeListener(MotorEditor.this);
					}
				}
				setLeftComponent(p);
			} else {
				setLeftComponent(new Editor(g));
			}
			// setDividerLocation(.25);
			// setResizeWeight(.25);
			if (g instanceof ChangeListening.Subject) {
				((ChangeListening.Subject) g)
						.addPropertyChangeListener(MotorEditor.this);
			}
		}
	}

	private class CaseEditor extends JSplitPane {
		private static final long serialVersionUID = 1L;

		public CaseEditor(Nozzle n, Chamber c) {
			super(JSplitPane.HORIZONTAL_SPLIT);
			setName("General Parameters");
			JPanel parts = new JPanel();
			parts.setLayout(new BoxLayout(parts, BoxLayout.Y_AXIS));
			setLeftComponent(parts);
			setRightComponent(new HardwarePanel(n, c));

			parts.add(new JLabel("Name:"));
			parts.add(new JTextField(motor.getName()) {
				private static final long serialVersionUID = 1L;
				{
					final JTextField t = this;
					addFocusListener(new FocusListener() {

						@Override
						public void focusLost(FocusEvent e) {
							String n = t.getText();
							if (!"".equals(n) && !n.equals(motor.getName())) {
								motor.setName(n);
							} else {
								t.setText(motor.getName());
							}
						}

						@Override
						public void focusGained(FocusEvent e) {

						}
					});

				}
			});
			parts.add(new JLabel("Fuel:"));
			parts.add( new JComboBox(availableFuels){{
				addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						motor.setFuel((Fuel)getSelectedItem());
						System.out.println("FUEL CHANGED");
					}});
			}});
			parts.add(new JLabel("Casing:"));
			parts.add(new Editor(c));
			parts.add(new JLabel("Nozzle:"));
			parts.add(new Editor(n));

			if (n instanceof ChangeListening.Subject) {
				((ChangeListening.Subject) n)
						.addPropertyChangeListener(MotorEditor.this);
			}
			if (c instanceof ChangeListening.Subject) {
				((ChangeListening.Subject) c)
						.addPropertyChangeListener(MotorEditor.this);
			}
		}
	}

	{
		text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);

	}

	public MotorEditor(Motor m, ComboBoxModel fuels) {
		super(JTabbedPane.BOTTOM);
		this.availableFuels = fuels;
		text.setName("XML");
		text.setEditable(false);
		//add(text, XML_TAB);
		setMotor(m, true);
	}

	public Motor getMotor() {
		return motor;
	}

	private void reText() {
		try {
			text.setText(MotorIO.writeMotor(motor));
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	private void setMotor(Motor m, boolean retext) {
		if (motor != null)
			motor.removePropertyChangeListener(this);
		motor = m;
		motor.addPropertyChangeListener(this);
		if (retext)
			reText();
		if (grainEditor != null)
			remove(grainEditor);
		while (getTabCount() > 1)
			removeTabAt(1);
		add(new CaseEditor(motor.getNozzle(), motor.getChamber()), CASING_TAB);
		add(new GrainEditor(motor.getGrain()), GRAIN_TAB);
		add(bt = new BurnTab(), BURN_TAB);
	}

	@Deprecated
	public static Motor defaultMotor() {
		Motor m = new Motor();
		m.setName("Example Motor");
		m.setFuel(new KNSU());

		CylindricalChamber c = new CylindricalChamber();
		c.setLength(Amount.valueOf(200, SI.MILLIMETER));
		c.setID(Amount.valueOf(30, SI.MILLIMETER));
		m.setChamber(c);

		CoredCylindricalGrain g = new CoredCylindricalGrain();
		try {
			g.setLength(Amount.valueOf(70, SI.MILLIMETER));
			g.setOD(Amount.valueOf(30, SI.MILLIMETER));
			g.setID(Amount.valueOf(10, SI.MILLIMETER));
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
	}

	public void addBurnWatcher(BurnWatcher bw) {
		burnWatchers.add(bw);
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
		Vector<Fuel> ff = new Vector<Fuel>();
		ff.add(new KNSU());
		//new MotorEditor(defaultMotor(), ff).showAsWindow();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		reText();
		// Dont re-burn for a name change!
		if (!evt.getPropertyName().equals("Name")){
			bt.reBurn();
		} else {
			for (BurnWatcher bw : burnWatchers)
				bw.replace(burn, burn);
		}
	}

}
