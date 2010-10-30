package com.billkuker.rocketry.motorsim.visual.workbench;

import com.billkuker.rocketry.motorsim.Burn;

public interface BurnWatcher {
	public void replace( Burn oldBurn, Burn newBurn);
}
