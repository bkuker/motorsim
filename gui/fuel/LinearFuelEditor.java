package fuel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.RocketScience;
import com.billkuker.rocketry.motorsim.fuel.editable.EditablePiecewiseLinearFuel;

public class LinearFuelEditor extends AbstractFuelEditor {
	private static final long serialVersionUID = 1L;

	private class Entry implements Comparable<Entry> {
		Amount<Pressure> p = Amount.valueOf(0, RocketScience.UnitPreference.getUnitPreference().getPreferredUnit(RocketScience.PSI));
		Amount<Velocity> v = Amount.valueOf(0, RocketScience.UnitPreference.getUnitPreference().getPreferredUnit(SI.METERS_PER_SECOND));

		@Override
		public int compareTo(Entry o) {
			return p.compareTo(o.p);
		}
	}

	private class TM extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public int getColumnCount() {
			return 2;
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
				return "Burn Rate";
			}
			return null;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Entry e = entries.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return RocketScience.ammountToString(e.p);
			case 1:
				return RocketScience.ammountToString(e.v);
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
					try {
						e.v = (Amount<Velocity>) Amount.valueOf((String) value);
					} catch ( Exception ee ){
						double d = Double.parseDouble((String)value);
						e.v = (Amount<Velocity>)Amount.valueOf(d, e.v.getUnit());
					}
					break;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			Collections.sort(entries);
			fireTableDataChanged();
			//f = new EditablePSRFuel(SaintRobertFuel.Type.NONSI);
			f.clear();
			for (Entry en : entries) {
				f.add(en.p, en.v);
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
	final EditablePiecewiseLinearFuel f;

	public LinearFuelEditor(EditablePiecewiseLinearFuel f) {
		super( f );
		this.f = f;
		for ( Map.Entry<Amount<Pressure>, Amount<Velocity>> e : f.getEntries().entrySet() ){
			Entry n = new Entry();
			n.p = e.getKey();
			n.v = e.getValue();
			entries.add(n);
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
		
		editBottom.setBottomComponent(controls);
		
		

		editBottom.setDividerLocation(.8);
		
		return editBottom;
	}

	public static void main(String args[]) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(new LinearFuelEditor(new EditablePiecewiseLinearFuel()));
		f.setSize(800, 600);
		f.setVisible(true);

	}

}
