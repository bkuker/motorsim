package com.billkuker.rocketry.motorsim.fuel;

import java.awt.Component;
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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.RocketScience;
import com.billkuker.rocketry.motorsim.fuel.SaintRobertFuel.Type;
import com.billkuker.rocketry.motorsim.fuel.editable.EditablePiecewiseSaintRobertFuel;

public class SRFuelEditor extends AbstractFuelEditor {
	private static final long serialVersionUID = 1L;

	private static final NumberFormat nf = new DecimalFormat("##########.###");

	private class Entry implements Comparable<Entry> {
		Amount<Pressure> p = Amount.valueOf(0, RocketScience.UnitPreference.getUnitPreference().getPreferredUnit(RocketScience.PSI));
		double a;
		double n;

		@Override
		public int compareTo(Entry o) {
			return p.compareTo(o.p);
		}
	}

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
	
	private Vector<Entry> entries = new Vector<Entry>();
	JPanel controls;
	final EditablePiecewiseSaintRobertFuel f;

	public SRFuelEditor(EditablePiecewiseSaintRobertFuel f) {
		super( f);
		this.f = f;
		
		for ( Amount<Pressure> p : f.getAMap().keySet() ){
			Entry e = new Entry();
			e.a = f.getAMap().get(p);
			e.n = f.getNMap().get(p);
			entries.add(e);
		}
		Collections.sort(entries);
		
	}
	
	protected  Component getBurnrateEditComponent(){
		final TM tm = new TM();

		JSplitPane editBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		

		JTable table = new JTable(tm);
		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setMinimumSize(new Dimension(200, 200));
		editBottom.setTopComponent(scrollpane);

		
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
		
		

		editBottom.setDividerLocation(.8);
		
		return editBottom;
	}

	public static void main(String args[]) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(new SRFuelEditor(new EditablePiecewiseSaintRobertFuel()));
		f.setSize(800, 600);
		f.setVisible(true);

	}

}
