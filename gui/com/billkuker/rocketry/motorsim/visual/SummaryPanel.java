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
import com.billkuker.rocketry.motorsim.RocketScience;

public class SummaryPanel extends JPanel implements Burn.BurnProgressListener {
	private static final long serialVersionUID = 1L;
	private static final Color RED = new Color(196, 0, 0);
	private static final Color GREEN = new Color(0, 196, 0);
	private static final Color ORANGE = new Color(160, 96, 0);
	private final Burn burn;
	private final JProgressBar bar = new JProgressBar();

	
	public SummaryPanel(Burn b) {
		setPreferredSize(new Dimension(100, 40));
		setLayout(new GridLayout(1, 1));
		bar.setStringPainted(true);
		add(bar);
		this.burn = b;
		burn.addBurnProgressListener(this);
	}

	@Override
	public void setProgress(float p) {
		int pct = (int) (p * 100);
		bar.setValue(pct);
		Amount<Length> web = burn.getMotor().getGrain().webThickness();
		Amount<Length> remaining = web.times(1.0 - p);
		if ( remaining.isLessThan(Amount.valueOf(0, SI.MILLIMETER))){
			remaining = Amount.valueOf(0, remaining.getUnit());
		}
		bar.setString("Burn Progress: " + pct + "% (" + RocketScience.ammountToRoundedString(remaining) + " web thickness remaining)");
	}

	@Override
	public void burnComplete() {
		setBurnSummary(new BurnSummary(burn));
	}

	private void setBurnSummary(final BurnSummary bi) {

		removeAll();
		setLayout(new GridLayout(2, 5));
		SwingUtilities.invokeLater(new Thread() {
			public void run() {
				SummaryPanel.this.add(new JLabel("Rating"));
				SummaryPanel.this.add(new JLabel("Total Impulse"));
				SummaryPanel.this.add(new JLabel("ISP"));
				SummaryPanel.this.add(new JLabel("Max Thrust"));
				SummaryPanel.this.add(new JLabel("Average Thust"));
				SummaryPanel.this.add(new JLabel("Max Pressure"));

				SummaryPanel.this.add(new JLabel("Safty Factor"));

				SummaryPanel.this.add(new JLabel(bi.getRating()));
				SummaryPanel.this.add(new JLabel(RocketScience
						.ammountToRoundedString(bi.totalImpulse())));
				SummaryPanel.this.add(new JLabel(RocketScience
						.ammountToRoundedString(bi.specificImpulse())));
				SummaryPanel.this.add(new JLabel(RocketScience
						.ammountToRoundedString(bi.maxThrust())));
				SummaryPanel.this.add(new JLabel(RocketScience
						.ammountToRoundedString(bi.averageThrust())));
				SummaryPanel.this.add(new JLabel(RocketScience
						.ammountToRoundedString(bi.maxPressure())));

				Color saftyColor;
				if (bi.getSaftyFactor() == null) {

					saftyColor = Color.BLACK;
					SummaryPanel.this.add(new JLabel("NA"));
				} else {
					double d = bi.getSaftyFactor();
					if (d >= 1.5) {
						saftyColor = GREEN;
					} else if (d > 1) {
						saftyColor = ORANGE;
					} else {
						saftyColor = RED;
					}
					JLabel l = new JLabel(new DecimalFormat("##########.##")
							.format(bi.getSaftyFactor()));
					l.setOpaque(true);
					l.setBackground(saftyColor);
					l.setForeground(Color.WHITE);
					SummaryPanel.this.add(l);
				}
				revalidate();
			}
		});
	}

}
