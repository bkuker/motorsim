package com.billkuker.rocketry.motorsim.visual.openRocket;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.measure.unit.SI;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.file.openrocket.OpenRocketLoader;
import net.sf.openrocket.gui.plot.PlotConfiguration;
import net.sf.openrocket.gui.plot.SimulationPlotDialog;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.ResourceBundleTranslator;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.RocketScience;
import com.billkuker.rocketry.motorsim.RocketScience.UnitPreference;
import com.billkuker.rocketry.motorsim.visual.workbench.BurnWatcher;

public class RocketSimTable extends JScrollPane implements BurnWatcher,
		RocketScience.UnitPreferenceListener {
	static final long serialVersionUID = 1L;

	static {
		Application.setBaseTranslator(new ResourceBundleTranslator(
				"l10n.messages"));
		Application.setMotorSetDatabase(new OneMotorDatabase());
		Application.setPreferences(new SwingPreferences());

	}

	class Entry {
		Entry(Motor m, Simulation s, Burn b) {
			this.m = m;
			this.s = s;
			this.b = b;
		}

		Simulation s;
		Motor m;
		Burn b;
	}

	List<Entry> entries = new Vector<Entry>();

	class TM extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Motor";
			case 1:
				return "Apogee";
			case 2:
				return "Vmax";
			case 3:
				return "Vdeploy";
			case 4:
				return "Vground";
			case 5:
				return "Tapogee";
			case 6:
				return "Tflight";
			}
			return "??";
		}

		@Override
		public int getColumnCount() {
			return 7;
		}

		@Override
		public int getRowCount() {
			return entries.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Entry e = entries.get(row);
			if (e == null)
				return "???";
			switch (col) {
			case 0:
				return e.m.getName();
			case 1:
				return RocketScience.ammountToRoundedString(Amount.valueOf(e.s
						.getSimulatedData().getMaxAltitude(), SI.METER));
			case 2:
				return RocketScience.ammountToRoundedString(Amount.valueOf(e.s
						.getSimulatedData().getMaxVelocity(),
						SI.METERS_PER_SECOND));
			case 3:
				return RocketScience.ammountToRoundedString(Amount.valueOf(e.s
						.getSimulatedData().getDeploymentVelocity(),
						SI.METERS_PER_SECOND));
			case 4:
				return RocketScience.ammountToRoundedString(Amount.valueOf(e.s
						.getSimulatedData().getGroundHitVelocity(),
						SI.METERS_PER_SECOND));
			case 5:
				return RocketScience.ammountToRoundedString(Amount.valueOf(e.s
						.getSimulatedData().getTimeToApogee(), SI.SECOND));
			case 6:
				return RocketScience.ammountToRoundedString(Amount.valueOf(e.s
						.getSimulatedData().getFlightTime(), SI.SECOND));
			}
			return "??";
		}

	}

	private TM tm = new TM();
	private JTable table = new JTable(tm);
	private OpenRocketDocument doc;

	public RocketSimTable() {
		setViewportView(table);
		RocketScience.addUnitPreferenceListener(this);
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					Entry entry = entries.get(table.getSelectedRow());
					SimulationPlotDialog.showPlot(SwingUtilities
							.getWindowAncestor(RocketSimTable.this), entry.s,
							PlotConfiguration.DEFAULT_CONFIGURATIONS[0].resetUnits());
				}
			}
		});
		preferredUnitsChanged();
	}

	public void openRocket(File f) throws RocketLoadException {
		this.doc = new OpenRocketLoader().load(f);
	}

	private Entry toEntry(Burn b) {
		Entry e = new Entry(b.getMotor(), doc.getSimulation(0).copy(), b);
		OneMotorDatabase.setBurn(b);

		e.s.getConfiguration().getMotorConfigurationID();
		Iterator<RocketComponent> iterator = doc.getRocket().iterator();
		while (iterator.hasNext()) {
			RocketComponent c = iterator.next();
			if (c instanceof MotorMount) {
				((MotorMount) c).setMotorDelay(e.s.getConfiguration()
						.getMotorConfigurationID(), b.getMotor()
						.getEjectionDelay().doubleValue(SI.SECOND));
			}
		}

		try {
			e.s.simulate();
		} catch (SimulationException e1) {
			e1.printStackTrace();
		}

		return e;
	}

	@Override
	public void replace(Burn oldBurn, Burn newBurn) {
		if (doc == null) {
			return; // TODO, deal with changing rockets
		}

		if (oldBurn != null) {
			for (int i = 0; i < entries.size(); i++) {
				if (entries.get(i).b == oldBurn) {
					if (newBurn != null) {
						entries.set(i, toEntry(newBurn));
					} else {
						entries.remove(i);
					}
					break;
				}
			}
		} else {
			entries.add(toEntry(newBurn));
		}
		tm.fireTableDataChanged();
	}

	@Override
	public void preferredUnitsChanged() {
		tm.fireTableDataChanged();
		if (UnitPreference.getUnitPreference() == UnitPreference.NONSI){
			System.err.println("NONSI");
			UnitGroup.setDefaultImperialUnits();
		} else {
			System.err.println("SI");
			UnitGroup.setDefaultMetricUnits();
		}
	}
}
