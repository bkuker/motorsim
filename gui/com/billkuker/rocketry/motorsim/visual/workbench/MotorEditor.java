package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
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

import org.apache.log4j.Logger;
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
import com.billkuker.rocketry.motorsim.RocketScience;
import com.billkuker.rocketry.motorsim.cases.Schedule40;
import com.billkuker.rocketry.motorsim.fuel.KNSU;
import com.billkuker.rocketry.motorsim.grain.CSlot;
import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;
import com.billkuker.rocketry.motorsim.grain.EndBurner;
import com.billkuker.rocketry.motorsim.grain.Finocyl;
import com.billkuker.rocketry.motorsim.grain.Moonburner;
import com.billkuker.rocketry.motorsim.grain.MultiGrain;
import com.billkuker.rocketry.motorsim.grain.RodAndTubeGrain;
import com.billkuker.rocketry.motorsim.grain.Star;
import com.billkuker.rocketry.motorsim.visual.BurnPanel;
import com.billkuker.rocketry.motorsim.visual.Editor;
import com.billkuker.rocketry.motorsim.visual.GrainPanel;
import com.billkuker.rocketry.motorsim.visual.HardwarePanel;

public class MotorEditor extends JTabbedPane implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(MotorEditor.class);
	Motor motor;
	GrainEditor grainEditor;
	BurnTab bt;
	Burn burn;

	private Vector<BurnWatcher> burnWatchers = new Vector<BurnWatcher>();
	private DefaultComboBoxModel availableFuels = new DefaultComboBoxModel();
	
	public void addFuel(Fuel f){
		availableFuels.addElement(f);
	}

	//private static final int XML_TAB = 0;
	private static final int CASING_TAB = 0;
	private static final int GRAIN_TAB = 1;
	private static final int BURN_TAB = 2;

	private List<Class<? extends Grain>> grainTypes = new Vector<Class<? extends Grain>>();
	{
		grainTypes.add(CoredCylindricalGrain.class);
		grainTypes.add(Finocyl.class);
		grainTypes.add(Star.class);
		grainTypes.add(Moonburner.class);
		grainTypes.add(RodAndTubeGrain.class);
		grainTypes.add(CSlot.class);
		grainTypes.add(EndBurner.class);
	}

	private abstract class Chooser<T> extends JPanel {
		private static final long serialVersionUID = 1L;
		private List<Class<? extends T>> types;
		private Map<Class<? extends T>, T> old = new HashMap<Class<? extends T>, T>();

		@SuppressWarnings("unchecked")
		public Chooser(T initial, List<Class<? extends T>> ts) {
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
					final JLabel progress = new JLabel();
					add(progress, BorderLayout.CENTER);
					try {
						final Burn b = new Burn(motor);
						b.addBurnProgressListener(
								new Burn.BurnProgressListener() {
									@Override
									public void setProgress(float f) {
										int pct = (int)(f*100);
										bar.setValue(pct);
										Amount<Length> web = motor.getGrain().webThickness();
										Amount<Length> remaining = web.times(1.0 - f);
										
										progress.setText("Progress: " + pct + "% (" + RocketScience.ammountToRoundedString(remaining) + " web thickness remaining)");
										if ( currentThread != me ){
											throw new BurnCanceled();
										}
									}
								});
						b.burn();

						final BurnPanel bp = new BurnPanel(b);
						SwingUtilities.invokeLater(new Thread() {
							public void run() {
								remove(bar);
								remove(progress);
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

	private class CaseEditor extends JSplitPane implements ComponentListener {
		private static final long serialVersionUID = 1L;

		public CaseEditor(Nozzle n, Chamber c) {
			super(JSplitPane.VERTICAL_SPLIT);
			setName("General Parameters");
			this.addComponentListener(this);
			
			JPanel parts = new JPanel();
			parts.setLayout(new BoxLayout(parts, BoxLayout.X_AXIS));
			setTopComponent(parts);
			setBottomComponent(new HardwarePanel(n, c));
			
			JPanel nameAndFuel = new JPanel();
			nameAndFuel.setLayout(new BoxLayout(nameAndFuel, BoxLayout.Y_AXIS));

			nameAndFuel.add(new JLabel("Name:"));
			nameAndFuel.add(new JTextField(motor.getName()) {
				private static final long serialVersionUID = 1L;
				{
					setMinimumSize(new Dimension(200, 20));
					setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
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
			nameAndFuel.add(new JLabel("Fuel:"));
			nameAndFuel.add( new JComboBox(availableFuels){
				{
					this.setSelectedItem(motor.getFuel());
				}
				private static final long serialVersionUID = 1L;
				{
					setMinimumSize(new Dimension(200, 20));
					setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
					addActionListener(new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {
							motor.setFuel((Fuel)getSelectedItem());
							System.out.println("FUEL CHANGED");
						}});
				}
			});
			nameAndFuel.add(Box.createVerticalGlue());
			parts.add(nameAndFuel);
			
			JPanel casing = new JPanel();
			casing.setLayout(new BoxLayout(casing, BoxLayout.Y_AXIS));
			casing.add(new JLabel("Casing:"));
			casing.add(new Editor(c));
			parts.add(casing);
			
			JPanel nozzle = new JPanel();
			nozzle.setLayout(new BoxLayout(nozzle, BoxLayout.Y_AXIS));
			nozzle.add(new JLabel("Nozzle:"));
			nozzle.add(new Editor(n));
			parts.add(nozzle);

			if (n instanceof ChangeListening.Subject) {
				((ChangeListening.Subject) n)
						.addPropertyChangeListener(MotorEditor.this);
			}
			if (c instanceof ChangeListening.Subject) {
				((ChangeListening.Subject) c)
						.addPropertyChangeListener(MotorEditor.this);
			}
		}

		@Override
		public void componentHidden(ComponentEvent arg0) {

		}

		@Override
		public void componentMoved(ComponentEvent arg0) {

		}

		@Override
		public void componentResized(ComponentEvent arg0) {
			setResizeWeight(.5);
			setDividerLocation(.5);
		}

		@Override
		public void componentShown(ComponentEvent arg0) {

		}
	}


	public MotorEditor(Motor m, Collection<Fuel> fuels) {
		super(JTabbedPane.BOTTOM);
		for ( Fuel f : fuels )
			addFuel(f);
		setMotor(m);
	}

	public Motor getMotor() {
		return motor;
	}


	private void setMotor(Motor m) {
		if (motor != null)
			motor.removePropertyChangeListener(this);
		motor = m;
		motor.addPropertyChangeListener(this);
		if (grainEditor != null)
			remove(grainEditor);
		while (getTabCount() > 1)
			removeTabAt(1);
		add(new CaseEditor(motor.getNozzle(), motor.getChamber()), CASING_TAB);
		add(new GrainEditor(motor.getGrain()), GRAIN_TAB);
		add(bt = new BurnTab(), BURN_TAB);
	}

	public static Motor defaultMotor() {
		Motor m = new Motor();
		m.setName("Example Motor");
		m.setFuel(new KNSU());

		CylindricalChamber c = new CylindricalChamber();
		c.setLength(Amount.valueOf(200, SI.MILLIMETER));
		c.setID(Amount.valueOf(30, SI.MILLIMETER));
		m.setChamber(c);

		Schedule40 pvc = new Schedule40();
		pvc.setLength(Amount.valueOf(200, SI.MILLIMETER));
		m.setChamber(pvc);
		
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
		// Dont re-burn for a name change!
		if (!evt.getPropertyName().equals("Name")){
			bt.reBurn();
		} else {
			for (BurnWatcher bw : burnWatchers)
				bw.replace(burn, burn);
		}
	}

}
