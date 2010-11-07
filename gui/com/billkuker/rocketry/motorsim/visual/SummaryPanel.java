package com.billkuker.rocketry.motorsim.visual;

import java.awt.Color;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.BurnSummary;
import com.billkuker.rocketry.motorsim.RocketScience;

public class SummaryPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Color RED = new Color(196, 0, 0);
	private static final Color GREEN = new Color(0, 196, 0);
	private static final Color ORANGE = new Color(160, 96, 0);

	public SummaryPanel(Burn b) {
		super(new GridLayout(2, 5));
	}

	public void setBurnSummary(BurnSummary bi) {
		{
			this.add(new JLabel("Rating"));
			this.add(new JLabel("Total Impulse"));
			this.add(new JLabel("ISP"));
			this.add(new JLabel("Max Thrust"));
			this.add(new JLabel("Average Thust"));
			this.add(new JLabel("Max Pressure"));

			this.add(new JLabel("Safty Factor"));

			this.add(new JLabel(bi.getRating()));
			this.add(new JLabel(RocketScience.ammountToRoundedString(bi
					.totalImpulse())));
			this.add(new JLabel(RocketScience.ammountToRoundedString(bi
					.specificImpulse())));
			this.add(new JLabel(RocketScience.ammountToRoundedString(bi
					.maxThrust())));
			this.add(new JLabel(RocketScience.ammountToRoundedString(bi
					.averageThrust())));
			this.add(new JLabel(RocketScience.ammountToRoundedString(bi
					.maxPressure())));

			Color saftyColor;
			if (bi.getSaftyFactor() == null) {

				saftyColor = Color.BLACK;
				this.add(new JLabel("NA"));
			} else {
				double d = bi.getSaftyFactor();
				if (d >= 1.5) {
					saftyColor = GREEN;
				} else if (d > 1) {
					saftyColor = ORANGE;
				} else {
					saftyColor = RED;
				}
				JLabel l = new JLabel(
						new DecimalFormat("##########.#").format(bi
								.getSaftyFactor()));
				l.setOpaque(true);
				l.setBackground(saftyColor);
				l.setForeground(Color.WHITE);
				this.add(l);
			}

		}
	}
}
