package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Vector;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.RocketScience;
import com.billkuker.rocketry.motorsim.fuel.EditableFuel.EditableCombustionProduct;
import com.billkuker.rocketry.motorsim.fuel.PiecewiseSaintRobertFuel;
import com.billkuker.rocketry.motorsim.fuel.SaintRobertFuel;
import com.billkuker.rocketry.motorsim.fuel.SaintRobertFuel.Type;
import com.billkuker.rocketry.motorsim.visual.Chart;
import com.billkuker.rocketry.motorsim.visual.Editor;

public class SRFuelEditor extends JSplitPane {
	private static final long serialVersionUID = 1L;

	private static final NumberFormat nf = new DecimalFormat("##########.###");

	Chart<Pressure, Velocity> burnRate;

	private class Entry implements Comparable<Entry> {
		Amount<Pressure> p = Amount.valueOf(0, RocketScience.UnitPreference.getUnitPreference().getPreferredUnit(RocketScience.PSI));
		double a;
		double n;

		@Override
		public int compareTo(Entry o) {
			return p.compareTo(o.p);
		}
	}

	public static class EditablePSRFuel extends PiecewiseSaintRobertFuel {

		@SuppressWarnings("unchecked")
		private Amount<VolumetricDensity> idealDensity = (Amount<VolumetricDensity>) Amount
				.valueOf("1 g/mm^3");
		
		private double combustionEfficiency = 1;
		private double densityRatio = 1;
		private EditableCombustionProduct cp;
		private String name = "New Fuel";

		public EditablePSRFuel(Type t) {
			super(t);
			cp = new EditableCombustionProduct();
		}
		
		public void clear(){
			super.clear();
		}
		
		public void setType(Type t){
			super.setType(t);
		}

		public void add(Amount<Pressure> p, final double _a, final double _n) {
			super.add(p, _a, _n);

		}

		public Amount<VolumetricDensity> getIdealDensity() {
			return idealDensity;
		}

		public void setIdealDensity(Amount<VolumetricDensity> idealDensity) {
			this.idealDensity = idealDensity;
		}

		public double getCombustionEfficiency() {
			return combustionEfficiency;
		}

		public void setCombustionEfficiency(double combustionEfficiency) {
			this.combustionEfficiency = combustionEfficiency;
		}

		public double getDensityRatio() {
			return densityRatio;
		}

		public void setDensityRatio(double densityRatio) {
			this.densityRatio = densityRatio;
		}

		@Override
		public CombustionProduct getCombustionProduct() {
			return cp;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

	final EditablePSRFuel f = new EditablePSRFuel(SaintRobertFuel.Type.SI);

	private class TM extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return entries.size();
		}

		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Pressure";
			case 1:
				return "Coefficient (a)";
			case 2:
				return "Exponent (n)";
			}
			return null;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Entry e = entries.get(rowIndex);
			switch (columnIndex) {
			case 0:
				//Format like 100 psi or 4.8 Mpa
				return nf.format(e.p.doubleValue(e.p.getUnit())) + " " + e.p.getUnit();
			case 1:
				return e.a;
			case 2:
				return e.n;
			}
			return null;
		}

		public boolean isCellEditable(int row, int col) {
			return true;
		}

		@SuppressWarnings("unchecked")
		public void setValueAt(Object value, int row, int col) {
			Entry e = entries.get(row);
			try {
				switch (col) {
				case 0:
					try {
						e.p = (Amount<Pressure>) Amount.valueOf((String) value);
					} catch ( Exception ee ){
						double d = Double.parseDouble((String)value);
						e.p = (Amount<Pressure>)Amount.valueOf(d, e.p.getUnit());
					}
					break;
				case 1:
					e.a = Double.valueOf((String) value);
					break;
				case 2:
					e.n = Double.valueOf((String) value);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			Collections.sort(entries);
			fireTableDataChanged();
			//f = new EditablePSRFuel(SaintRobertFuel.Type.NONSI);
			f.clear();
			for (Entry en : entries) {
				f.add(en.p, en.a, en.n);
			}
			f.firePropertyChange(new PropertyChangeEvent(f,"entries", null, null));

			update();

		}

		@Override
		public void fireTableDataChanged() {
			super.fireTableDataChanged();
		}

	};
	
	public Fuel getFuel(){
		return f;
	}

	private void update() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				editTop.setTopComponent(new Editor(f));
				editTop.setBottomComponent(new Editor(f.getCombustionProduct()));
				if (burnRate != null)
					SRFuelEditor.this.remove(burnRate);
				try {
					burnRate = new Chart<Pressure, Velocity>(
							SI.MEGA(SI.PASCAL), SI.MILLIMETER.divide(SI.SECOND)
									.asType(Velocity.class), f, "burnRate");
				} catch (NoSuchMethodException e) {
					throw new Error(e);
				}
				burnRate.setDomain(burnRate.new IntervalDomain(Amount.valueOf(
						0, SI.MEGA(SI.PASCAL)), Amount.valueOf(11, SI
						.MEGA(SI.PASCAL)), 50));
				SRFuelEditor.this.setRightComponent(burnRate);
				SRFuelEditor.this.revalidate();
			}
		});
	}

	private Vector<Entry> entries = new Vector<Entry>();

	JSplitPane editParent;
	JSplitPane editTop;
	JSplitPane editBottom;
	JPanel controls;

	public SRFuelEditor() {
		super(HORIZONTAL_SPLIT);
		setResizeWeight(0);
		setDividerLocation(.3);
		
		final TM tm = new TM();

		editParent = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		editTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		editBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		editParent.setTopComponent(editTop);
		editParent.setBottomComponent(editBottom);
		
		editTop.setTopComponent(new Editor(f));

		JTable table = new JTable(tm);
		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setMinimumSize(new Dimension(200, 200));
		editBottom.setTopComponent(scrollpane);

		setLeftComponent(editParent);

		JButton add = new JButton("Add Data");
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				entries.add(new Entry());
				tm.fireTableDataChanged();
			}
		});
		controls = new JPanel();
		controls.setPreferredSize(new Dimension(200, 50));
		controls.setLayout(new FlowLayout());
			
		controls.add(add);

		
		final JRadioButton si, nonsi;
		ButtonGroup type = new ButtonGroup();
		JPanel radio = new JPanel();
		radio.add(si = new JRadioButton("SI"));
		radio.add(nonsi = new JRadioButton("NonSI"));
		controls.add(radio);
		type.add(si);
		type.add(nonsi);

		si.setSelected(true);
		
		si.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				if ( si.isSelected() ){
					System.err.println("SI");
					f.setType(Type.SI);
				} else {
					System.err.println("NONSI");
					f.setType(Type.NONSI);
				}
				update();
			}});
		
		editBottom.setBottomComponent(controls);
		
		
		editParent.setDividerLocation(.5);
		editTop.setDividerLocation(.5);
		editBottom.setDividerLocation(.8);
		
		editParent.resetToPreferredSizes();
		revalidate();

		update();
	}

	public static void main(String args[]) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(new SRFuelEditor());
		f.setSize(800, 600);
		f.setVisible(true);

	}

}
