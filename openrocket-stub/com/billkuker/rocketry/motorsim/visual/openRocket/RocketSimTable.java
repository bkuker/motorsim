package com.billkuker.rocketry.motorsim.visual.openRocket;

import java.io.File;

import javax.swing.JPanel;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.RocketScience;
import com.billkuker.rocketry.motorsim.visual.workbench.BurnWatcher;

public class RocketSimTable extends JPanel implements BurnWatcher, RocketScience.UnitPreferenceListener {
	static final long serialVersionUID = 1L;

	@Override
	public void preferredUnitsChanged() {

	}

	@Override
	public void replace(Burn oldBurn, Burn newBurn) {

	}

	public void openRocket(File f) {

	}

}
