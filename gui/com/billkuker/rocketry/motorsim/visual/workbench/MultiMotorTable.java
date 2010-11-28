package com.billkuker.rocketry.motorsim.visual.workbench;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.BurnSummary;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.RocketScience;

public class MultiMotorTable extends JTable implements BurnWatcher, RocketScience.UnitPreferenceListener {
	private static final long serialVersionUID = 1L;

	private class Entry {
		Motor m;
		BurnSummary bs;
	}

	private List<Entry> entries = new Vector<Entry>();

	private class TM extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Motor";
			case 1:
				return "Fuel";
			case 2:
				return "Rating";
			case 3:
				return "Total Impulse";
			case 4:
				return "ISP";
			case 5:
				return "Max Thrust";
			case 6:
				return "Average Thrust";
			case 7:
				return "Max Pressure";
			case 8:
				return "Volume Loading";
			case 9:
				return "Fuel Mass";
			}
			return null;
		}
		
		@Override
		public int getColumnCount() {
			return 10;
		}

		@Override
		public int getRowCount() {
			return entries.size();
		}

		@Override
		public Object getValueAt(int rowIdx, int col) {
			if (rowIdx >= entries.size() || rowIdx < 0)
				throw new IndexOutOfBoundsException("Row out of bounds");
			BurnSummary bs = entries.get(rowIdx).bs;
			Motor m = entries.get(rowIdx).m;
			switch (col) {
			case 0:
				return m.getName();
			case 1:
				return m.getFuel().getName();
			case 2:
				return bs.getRating();
			case 3:
				return RocketScience.ammountToRoundedString(bs.totalImpulse());
			case 4:
				return RocketScience.ammountToRoundedString(bs.specificImpulse());
			case 5:
				return RocketScience.ammountToRoundedString(bs.maxThrust());
			case 6:
				return RocketScience.ammountToRoundedString(bs.averageThrust());
			case 7:
				return RocketScience.ammountToRoundedString(bs.maxPressure());
			case 8:
				return Integer.toString((int)(bs.getVolumeLoading()*100.0)) + "%";
			case 9:
				return RocketScience.ammountToRoundedString(bs.getPropellantMass());
			}
			throw new IndexOutOfBoundsException("Col out of bounds");
		}

	}

	private TM tm = new TM();

	public MultiMotorTable() {
		setModel(tm);
		RocketScience.addUnitPreferenceListener(this);
	}

	public void addBurn(Burn b) {
		Entry e = new Entry();
		e.m = b.getMotor();
		e.bs = new BurnSummary(b);
		entries.add(e);
		tm.fireTableDataChanged();
	}

	public void removeBurn(Burn b) {
		Iterator<Entry> i = entries.iterator();
		while (i.hasNext()) {
			if (i.next().m == b.getMotor())
				i.remove();
		}
		tm.fireTableDataChanged();
	}

	@Override
	public void replace(Burn oldBurn, Burn newBurn) {
		if (oldBurn != null)
			removeBurn(oldBurn);
		if (newBurn != null)
			addBurn(newBurn);
	}

	@Override
	public void preferredUnitsChanged() {
		tm.fireTableDataChanged();
	}
}
