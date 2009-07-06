package com.billkuker.rocketry.motorsim.visual;

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

import com.billkuker.rocketry.motorsim.Chamber;
import com.billkuker.rocketry.motorsim.ConvergentDivergentNozzle;
import com.billkuker.rocketry.motorsim.CylindricalChamber;
import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.MotorPart;
import com.billkuker.rocketry.motorsim.Nozzle;
import com.billkuker.rocketry.motorsim.fuel.KNSU;
import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;
import com.billkuker.rocketry.motorsim.grain.Finocyl;
import com.billkuker.rocketry.motorsim.grain.Moonburner;
import com.billkuker.rocketry.motorsim.grain.MultiGrain;
import com.billkuker.rocketry.motorsim.grain.RodAndTubeGrain;
import com.billkuker.rocketry.motorsim.io.MotorIO;

public class MotorEditor extends JTabbedPane implements PropertyChangeListener, DocumentListener{
	private static final long serialVersionUID = 1L;
	RSyntaxTextArea text = new RSyntaxTextArea();
	Motor motor;
	GrainEditor grainEditor;
	
	private class GrainChooser extends JPanel{
		private static final long serialVersionUID = 1L;
		@SuppressWarnings("unchecked")
		private Class[] types = {
					CoredCylindricalGrain.class,
					Finocyl.class,
					Moonburner.class,
					RodAndTubeGrain.class
				};
		@SuppressWarnings("unchecked")
		public GrainChooser(){
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			for ( final Class c : types ){
				JButton b = new JButton(c.getSimpleName());
				add(b);
				b.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						try {
							grainChosen((Grain)c.newInstance());
						} catch (InstantiationException e1) {
							e1.printStackTrace();
						} catch (IllegalAccessException e1) {
							e1.printStackTrace();
						}
					}
				});
			}
		}
		protected void grainChosen(Grain g){
			System.out.println(g);
		}
	}
	
	private class GrainEditor extends JSplitPane{
		private static final long serialVersionUID = 1L;

		public GrainEditor(final Grain g){
			super(JSplitPane.HORIZONTAL_SPLIT);
			setRightComponent(new GrainPanel(g));
			if ( g instanceof Grain.Composite ){
				final JPanel p = new JPanel();
				p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
				p.add(new Editor(g));
				for ( Grain gg : ((Grain.Composite)g).getGrains() ){
					final int grainEditorIndex = p.getComponentCount() + 1;
					p.add(new GrainChooser(){
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;

						@Override
						protected void grainChosen(Grain ng){
							if ( g instanceof MultiGrain ){
								((MultiGrain) g).setGrain(ng);
								p.remove(grainEditorIndex);
								p.add(new Editor(ng), grainEditorIndex);
								p.remove(0);
								p.add(new Editor(g), 0);
								//System.out.println("Chose new grain");
							}
						}
					});
					p.add(new Editor(gg));
					if ( gg instanceof MotorPart ){
						((MotorPart)gg).addPropertyChangeListener(MotorEditor.this);
					}
				}
				setLeftComponent(p);
			} else {
				setLeftComponent(new Editor(g));
			}
			setDividerLocation(.25);
			setResizeWeight(.25);
			if ( g instanceof MotorPart ){
				((MotorPart)g).addPropertyChangeListener(MotorEditor.this);
			}
		}
	}
	
	private class FuelEditor extends JSplitPane{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public FuelEditor(Fuel f){
			super(JSplitPane.HORIZONTAL_SPLIT);
			Chart<Pressure, Velocity> burnRate;
			try {
				burnRate = new Chart<Pressure, Velocity>(
						SI.MEGA(SI.PASCAL),
						SI.METERS_PER_SECOND,
						f,
						"burnRate");
			} catch (NoSuchMethodException e) {
				throw new Error(e);
			}
			burnRate.setDomain(
					burnRate.new IntervalDomain(
							Amount.valueOf(0, SI.MEGA(SI.PASCAL)),
							Amount.valueOf(11, SI.MEGA(SI.PASCAL)),
							20
							));

			setLeftComponent(new Editor(f));
			setRightComponent(burnRate);
			setDividerLocation(.25);
			setResizeWeight(.25);
			if ( f instanceof MotorPart ){
				((MotorPart)f).addPropertyChangeListener(MotorEditor.this);
			}
		}
	}
	
	private class CaseEditor extends JSplitPane{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public CaseEditor(Nozzle n, Chamber c){
			super(JSplitPane.HORIZONTAL_SPLIT, 
					new JSplitPane(JSplitPane.VERTICAL_SPLIT,
							new Editor(n), new Editor(c)), new NozzlePanel(n));
			JSplitPane parts = (JSplitPane)getLeftComponent();
			parts.setDividerLocation(.5);
			parts.setResizeWeight(.5);
			setDividerLocation(.25);
			setResizeWeight(.25);

			if ( n instanceof MotorPart ){
				((MotorPart)n).addPropertyChangeListener(MotorEditor.this);
			}
			if ( c instanceof MotorPart ){
				((MotorPart)c).addPropertyChangeListener(MotorEditor.this);
			}
		}
	}
	
	{
		text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);

	}
	
	public MotorEditor(Motor m){
		super(JTabbedPane.BOTTOM);
		text.getDocument().addDocumentListener(this);
		addTab("XML", text);
		setMotor(m, true);
	}
	
	private void reText(){
		try {
			text.setText(MotorIO.writeMotor(motor));
		} catch (IOException e) {
			throw new Error(e);
		}
	}
	
	private void setMotor(Motor m, boolean retext){
		motor = m;
		if ( retext )
			reText();
		if ( grainEditor != null)
			remove(grainEditor);
	while ( getTabCount() > 1	)
		removeTabAt(1);
		add("Casing", new CaseEditor(motor.getNozzle(), motor.getChamber()));
		add("Grain", new GrainEditor(motor.getGrain()));
		add("Fuel", new FuelEditor(motor.getFuel()));
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

		m.setGrain(new MultiGrain(g,2));


		ConvergentDivergentNozzle n = new ConvergentDivergentNozzle();
		n.setThroatDiameter(Amount.valueOf(7.962, SI.MILLIMETER));
		n.setExitDiameter(Amount.valueOf(13.79, SI.MILLIMETER));
		n.setEfficiency(.85);
		m.setNozzle(n);

		return m;
	}
	
	@Deprecated
	public void showAsWindow(){
		JFrame f = new JFrame();
		f.setSize(1024,768);
		f.setContentPane(this);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setVisible(true);
	}
	
	public static void main(String args[]) throws Exception{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		new MotorEditor(defaultMotor()).showAsWindow();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		reText();
	}


	public void changedUpdate(DocumentEvent e) {
		try {
			final Motor m = MotorIO.readMotor(text.getText());
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					setMotor(m, false);
				}});
			System.out.println("Motor Updated");
		} catch (Throwable e1) {
			//Leave blank, motor might be broken
		}
	}

	public void insertUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
	}

	public void removeUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
	}
}
