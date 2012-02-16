package com.billkuker.rocketry.motorsim.visual;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.BurnSummary;
import com.billkuker.rocketry.motorsim.Colors;
import com.billkuker.rocketry.motorsim.RocketScience;

public class SummaryPanel extends JPanel implements Burn.BurnProgressListener, RocketScience.UnitPreferenceListener {
	private static final long serialVersionUID = 1L;

	private final Burn burn;
	private final JProgressBar bar = new JProgressBar();
	private BurnSummary bs;

	
	public SummaryPanel(Burn b) {
		setPreferredSize(new Dimension(100, 40));
		setLayout(new GridLayout(1, 1));
		bar.setStringPainted(true);
		add(bar);
		this.burn = b;
		burn.addBurnProgressListener(this);
	}

	@Override
	public void setProgress(final float p) {
		SwingUtilities.invokeLater(new Thread() {
			public void run() {
				int pct = (int) (p * 100);
				bar.setValue(pct);
				Amount<Length> web = burn.getMotor().getGrain().webThickness();
				Amount<Length> remaining = web.times(1.0 - p);
				if (remaining.isLessThan(Amount.valueOf(0, SI.MILLIMETER))) {
					remaining = Amount.valueOf(0, remaining.getUnit());
				}
				bar.setString("Burn Progress: " + pct + "% ("
						+ RocketScience.ammountToRoundedString(remaining)
						+ " web thickness remaining)");
			}
		});
	}

	@Override
	public void burnComplete() {
		setBurnSummary(bs = new BurnSummary(burn));
		RocketScience.addUnitPreferenceListener(this);
	}
	

	@Override
	public void preferredUnitsChanged() {
		if ( bs != null )
			setBurnSummary(bs);
	}

	private void setBurnSummary(final BurnSummary bi) {
		SwingUtilities.invokeLater(new Thread() {
			public void run() {
				removeAll();
				setLayout(new GridLayout(2, 5));
				add(new JLabel("Rating"));
				add(new JLabel("Total Impulse"));
				add(new JLabel("ISP"));
				add(new JLabel("Max Thrust"));
				add(new JLabel("Average Thust"));
				add(new JLabel("Max Pressure"));
				add(new JLabel("Fuel Mass"));
				add(new JLabel("Volume Loading"));
				add(new JLabel("Safty Factor"));

				add(new JLabel(bi.getRating()));
				add(new JLabel(RocketScience.ammountToRoundedString(bi
						.totalImpulse())));
				add(new JLabel(RocketScience.ammountToRoundedString(bi
						.specificImpulse())));
				add(new JLabel(RocketScience.ammountToRoundedString(bi
						.maxThrust())));
				add(new JLabel(RocketScience.ammountToRoundedString(bi
						.averageThrust())));
				add(new JLabel(RocketScience.ammountToRoundedString(bi
						.maxPressure())));
				add(new JLabel(RocketScience.ammountToRoundedString(bi
						.getPropellantMass())));
				add(new JLabel(
						Integer.toString((int) (bi.getVolumeLoading() * 100.0))
								+ "%"));

				Color saftyColor;
				if (bi.getSaftyFactor() == null) {

					saftyColor = Color.BLACK;
					add(new JLabel("NA"));
				} else {
					double d = bi.getSaftyFactor();
					if (d >= 1.5) {
						saftyColor = Colors.GREEN;
					} else if (d > 1) {
						saftyColor = Colors.ORANGE;
					} else {
						saftyColor = Colors.RED;
					}
					JLabel l = new JLabel(new DecimalFormat("##########.##")
							.format(bi.getSaftyFactor()));
					l.setOpaque(true);
					l.setBackground(saftyColor);
					l.setForeground(Color.WHITE);
					add(l);
				}
				revalidate();
				repaint();
			}
		});
	}

}
