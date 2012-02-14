package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.net.URI;
import java.util.List;
import java.util.Vector;

import javax.measure.quantity.Duration;
import javax.measure.unit.SI;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
import com.billkuker.rocketry.motorsim.Colors;
import com.billkuker.rocketry.motorsim.ConvergentDivergentNozzle;
import com.billkuker.rocketry.motorsim.CylindricalChamber;
import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.Nozzle;
import com.billkuker.rocketry.motorsim.cases.Schedule40;
import com.billkuker.rocketry.motorsim.cases.Schedule80;
import com.billkuker.rocketry.motorsim.fuel.FuelResolver;
import com.billkuker.rocketry.motorsim.fuel.KNSU;
import com.billkuker.rocketry.motorsim.grain.CSlot;
import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;
import com.billkuker.rocketry.motorsim.grain.EndBurner;
import com.billkuker.rocketry.motorsim.grain.Finocyl;
import com.billkuker.rocketry.motorsim.grain.Moonburner;
import com.billkuker.rocketry.motorsim.grain.MultiGrain;
import com.billkuker.rocketry.motorsim.grain.MultiPort;
import com.billkuker.rocketry.motorsim.grain.RodAndTubeGrain;
import com.billkuker.rocketry.motorsim.grain.Square;
import com.billkuker.rocketry.motorsim.grain.Star;
import com.billkuker.rocketry.motorsim.visual.BurnPanel;
import com.billkuker.rocketry.motorsim.visual.ClassChooser;
import com.billkuker.rocketry.motorsim.visual.Editor;
import com.billkuker.rocketry.motorsim.visual.GrainPanel;
import com.billkuker.rocketry.motorsim.visual.HardwarePanel;
import com.billkuker.rocketry.motorsim.visual.SummaryPanel;

public class MotorEditor extends JPanel implements PropertyChangeListener, FuelResolver.FuelsChangeListener{
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(MotorEditor.class);
	Motor motor;
	GrainEditor grainEditor;
	BurnTab bt;
	Burn burn;
	SummaryPanel sp;
	JTextArea error;
	JTabbedPane tabs;

	private Vector<BurnWatcher> burnWatchers = new Vector<BurnWatcher>();
	private DefaultComboBoxModel availableFuels = new DefaultComboBoxModel();
	
	public MotorEditor(Motor m) {
		setLayout( new BorderLayout());
		tabs = new JTabbedPane(JTabbedPane.TOP);
		add(tabs, BorderLayout.CENTER);
		availableFuels.addElement(m.getFuel());
		availableFuels.setSelectedItem(m.getFuel());
		FuelResolver.addFuelsChangeListener(this);
		fuelsChanged();
		setMotor(m);
		
		Burn.getBurnSettings().addPropertyChangeListener(this);
	}

	@Override
	public void fuelsChanged() {
		while ( availableFuels.getSize() > 0 && availableFuels.getIndexOf(availableFuels.getSelectedItem()) != 0 )
			availableFuels.removeElementAt(0);
		while ( availableFuels.getSize() > 1 )
			availableFuels.removeElementAt(1);
		for ( Fuel f : FuelResolver.getFuelMap().values() ){
			if ( f != availableFuels.getSelectedItem() )
				availableFuels.addElement(f);
		}
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
		grainTypes.add(MultiPort.class);
		grainTypes.add(Square.class);
	}
	
	private List<Class<? extends Chamber>> chamberTypes = new Vector<Class<? extends Chamber>>();
	{
		chamberTypes.add(CylindricalChamber.class);
		chamberTypes.add(Schedule40.class);
		chamberTypes.add(Schedule80.class);
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
			if ( error != null ){
				MotorEditor.this.remove(error);
				error = null;
			}
			if ( sp != null ){
				MotorEditor.this.remove(sp);
				sp = null;
			}
			currentThread = new Thread() {
				{
					setName("Burn " + motor.getName());
					setDaemon(true);
				}
				public void run() {
					final Thread me = this;
					try {						
						final Burn b = new Burn(motor);
						b.addBurnProgressListener(
								new Burn.BurnProgressListener() {
									@Override
									public void burnComplete(){};
									@Override
									public void setProgress(float f) {
										if ( currentThread != me ){
											throw new BurnCanceled();
										}
									}
								});

						MotorEditor.this.add(sp = new SummaryPanel(b), BorderLayout.NORTH);
						revalidate();
						b.burn();

						final BurnPanel bp = new BurnPanel(b);
						SwingUtilities.invokeLater(new Thread() {
							public void run() {
								add(bp, BorderLayout.CENTER);
								for (BurnWatcher bw : burnWatchers)
									bw.replace(burn, b);
								burn = b;
								revalidate();
							}
						});
					} catch (BurnCanceled c){
						log.info("Burn Canceled!");
					} catch (final Exception e) {
						SwingUtilities.invokeLater(new Thread() {
							public void run() {
								if ( sp != null )
									MotorEditor.this.remove(sp);
								error = new JTextArea(e.getMessage());
								error.setBackground(Colors.RED);
								error.setForeground(Color.WHITE);
								error.setEditable(false);
								MotorEditor.this.add(error, BorderLayout.NORTH);
								revalidate();
							}
						});
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
				
				Editor grainEditor = new Editor(g);
				grainEditor.setAlignmentX(LEFT_ALIGNMENT);
				p.add(grainEditor);
				
				for (Grain gg : ((Grain.Composite) g).getGrains()) {
					final int grainEditorIndex = p.getComponentCount() + 2;
					
					JLabel l = new JLabel("Grain Type:");
					l.setAlignmentX(LEFT_ALIGNMENT);
					p.add(l);
					
					p.add(new ClassChooser<Grain>(grainTypes, gg) {
						private static final long serialVersionUID = 1L;
						{setAlignmentX(LEFT_ALIGNMENT);}
						@Override
						protected Grain classSelected(
								Class<? extends Grain> clazz, Grain ng) {
							if ( ng == null ){
								try {
									ng = clazz.newInstance();
								} catch (InstantiationException e) {
									e.printStackTrace();
								} catch (IllegalAccessException e) {
									e.printStackTrace();
								}
							}
							if (g instanceof MultiGrain) {
								((MultiGrain) g).setGrain(ng);
								p.remove(grainEditorIndex);
								p.add(new Editor(ng), grainEditorIndex);
								p.remove(0);
								p.add(new Editor(g), 0);
							}
							return ng;

						}
					});
					
					Editor ggEditor = new Editor(gg);
					ggEditor.setAlignmentX(LEFT_ALIGNMENT);
					p.add(ggEditor);
					
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
		
		private HardwarePanel hp;
		private JPanel casing;
		private JPanel nozzle;
		private Editor casingEditor;
		private Editor nozzleEditor;
		
		private void setup() {
			if (casingEditor != null)
				casing.remove(casingEditor);
			casingEditor = new Editor(motor.getChamber());
			casingEditor.setAlignmentX(LEFT_ALIGNMENT);
			casing.add(casingEditor);
			
			if (nozzleEditor != null)
				nozzle.remove(nozzleEditor);
			nozzleEditor = new Editor(motor.getNozzle());
			nozzleEditor.setAlignmentX(LEFT_ALIGNMENT);
			nozzle.add(nozzleEditor);
			
			if (hp != null)
				remove(hp);
			setBottomComponent(hp = new HardwarePanel(motor));
			if (motor.getNozzle() instanceof ChangeListening.Subject) {
				((ChangeListening.Subject) motor.getNozzle())
						.addPropertyChangeListener(MotorEditor.this);
			}
			if (motor.getChamber() instanceof ChangeListening.Subject) {
				((ChangeListening.Subject) motor.getChamber())
						.addPropertyChangeListener(MotorEditor.this);
			}
			if (motor.getFuel() instanceof ChangeListening.Subject ){
				((ChangeListening.Subject) motor.getFuel())
						.addPropertyChangeListener(MotorEditor.this);
			}
		}

		public CaseEditor() {
			super(JSplitPane.VERTICAL_SPLIT);
			setName("General Parameters");
			this.addComponentListener(this);
			
			JPanel parts = new JPanel();
			parts.setLayout(new BoxLayout(parts, BoxLayout.X_AXIS));
			setTopComponent(parts);
			
			JPanel nameAndFuel = new JPanel();
			nameAndFuel.setLayout(new BoxLayout(nameAndFuel, BoxLayout.Y_AXIS));

			JLabel l = new JLabel("Name:");
			l.setAlignmentX(LEFT_ALIGNMENT);
			nameAndFuel.add(l);
			nameAndFuel.add(new JTextField(motor.getName()) {
				private static final long serialVersionUID = 1L;
				{
					setAlignmentX(LEFT_ALIGNMENT);
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
			
			l = new JLabel("Fuel:");
			l.setAlignmentX(LEFT_ALIGNMENT);
			nameAndFuel.add(l);
			
			nameAndFuel.add( new JComboBox(availableFuels){
				private static final long serialVersionUID = 1L;
				{
					setAlignmentX(LEFT_ALIGNMENT);
					this.setSelectedItem(motor.getFuel());
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
			
			l = new JLabel("Casing:");
			l.setAlignmentX(LEFT_ALIGNMENT);
			nameAndFuel.add(l);
			
			nameAndFuel.add(new ClassChooser<Chamber>(chamberTypes, motor.getChamber()) {
				private static final long serialVersionUID = 1L;
				{
					setAlignmentX(LEFT_ALIGNMENT);
					setMinimumSize(new Dimension(200, 20));
					setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
				}
				@Override
				protected Chamber classSelected(Class<? extends Chamber> clazz, Chamber c) {
					try {
						if ( c != null ){
							motor.setChamber(c);
						} else {
							motor.setChamber(clazz.newInstance());
						}
						return motor.getChamber();
					} catch (InstantiationException e) {
						log.error(e);
					} catch (IllegalAccessException e) {
						log.error(e);
					}
					return null;
				}
			});
			
			
			
			
			l = new JLabel("Delay:");
			l.setAlignmentX(LEFT_ALIGNMENT);
			nameAndFuel.add(l);
			nameAndFuel.add(new JTextField(Double.toString(motor.getEjectionDelay().doubleValue(SI.SECOND))) {
				private static final long serialVersionUID = 1L;
				{
					setAlignmentX(LEFT_ALIGNMENT);
					setMinimumSize(new Dimension(200, 20));
					setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
					final JTextField t = this;
					addFocusListener(new FocusListener() {

						@Override
						public void focusLost(FocusEvent e) {
							try {
								String n = t.getText();
								double d = Double.parseDouble(n);
								Amount<Duration> delay = Amount.valueOf(d, SI.SECOND);
								if ( delay != motor.getEjectionDelay() ){
									motor.setEjectionDelay(delay);
								}
							} catch ( Exception ex ){
								log.warn(e);
								setText(Double.toString(motor.getEjectionDelay().doubleValue(SI.SECOND)));
							}
						}

						@Override
						public void focusGained(FocusEvent e) {

						}
					});

				}
			});
			
			
			
			nameAndFuel.add(Box.createVerticalGlue());
			parts.add(nameAndFuel);
			
			casing = new JPanel();
			casing.setLayout(new BoxLayout(casing, BoxLayout.Y_AXIS));
			l = new JLabel("Casing:");
			l.setAlignmentX(LEFT_ALIGNMENT);
			casing.add(l);
			parts.add(casing);
			
			nozzle = new JPanel();
			nozzle.setLayout(new BoxLayout(nozzle, BoxLayout.Y_AXIS));
			l = new JLabel("Nozzle:");
			l.setAlignmentX(LEFT_ALIGNMENT);
			nozzle.add(l);
			parts.add(nozzle);
			
			motor.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent arg0) {
					setup();
					setResizeWeight(.5);
					setDividerLocation(.5);
				}
			});
			
			setup();
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
		while (tabs.getTabCount() > 1)
			tabs.removeTabAt(1);
		tabs.add(new CaseEditor(), CASING_TAB);
		tabs.add(new GrainEditor(motor.getGrain()), GRAIN_TAB);
		tabs.add(bt = new BurnTab(), BURN_TAB);
	}

	private static int idx;
	public static Motor defaultMotor() {
		Motor m = new Motor();
		m.setName("New Motor " + ++idx);
		try {
			m.setFuel(FuelResolver.getFuel(new URI("motorsim:KNDX")));
		} catch (Exception e) {
			throw new Error(e);
		}

		CylindricalChamber c = new CylindricalChamber();
		c.setLength(Amount.valueOf(420, SI.MILLIMETER));
		c.setID(Amount.valueOf(70, SI.MILLIMETER));
		c.setOD(Amount.valueOf(72, SI.MILLIMETER));
		m.setChamber(c);

		CoredCylindricalGrain g = new CoredCylindricalGrain();
		try {
			g.setLength(Amount.valueOf(100, SI.MILLIMETER));
			g.setOD(Amount.valueOf(62, SI.MILLIMETER));
			g.setID(Amount.valueOf(20, SI.MILLIMETER));
		} catch (PropertyVetoException v) {
			throw new Error(v);
		}
		
		MultiGrain mg = new MultiGrain(g, 4);
		mg.setSpacing(Amount.valueOf(6, SI.MILLIMETER));
		m.setGrain(mg);

		ConvergentDivergentNozzle n = new ConvergentDivergentNozzle();
		n.setThroatDiameter(Amount.valueOf(14.089, SI.MILLIMETER));
		n.setExitDiameter(Amount.valueOf(44.55, SI.MILLIMETER));
		n.setEfficiency(.85);
		m.setNozzle(n);

		return m;
	}

	public void focusOnObject(Object o) {
		if (o instanceof Grain)
			tabs.setSelectedIndex(GRAIN_TAB);
		if (o instanceof Chamber || o instanceof Nozzle)
			tabs.setSelectedIndex(CASING_TAB);
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
