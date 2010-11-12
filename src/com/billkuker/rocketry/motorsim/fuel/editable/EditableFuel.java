package com.billkuker.rocketry.motorsim.fuel.editable;

import java.net.URI;

import com.billkuker.rocketry.motorsim.Fuel;

public interface EditableFuel extends Fuel{
	public void setName(String name);
	public void setURI(URI uri);
}
