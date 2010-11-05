package com.billkuker.rocketry.motorsim.fuel;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.billkuker.rocketry.motorsim.Fuel;

public class FuelResolver {
	public static class FuelNotFound extends Exception {
		private static final long serialVersionUID = 1L;
	}

	private static Map<URI, Fuel> fuels = new HashMap<URI, Fuel>();

	static {
		add(new KNSB());
		add(new KNDX());
		add(new KNSU());
		add(new KNER());
	}
	
	public static Map<URI, Fuel> getFuelMap(){
		return Collections.unmodifiableMap(fuels);
	}

	public static Fuel getFuel(URI u) throws FuelNotFound {
		if (fuels.containsKey(u))
			return fuels.get(u);
		return tryResolve(u);
	}

	private static Fuel tryResolve(URI u) throws FuelNotFound {
		throw new FuelNotFound();
	}

	private static void add(Fuel f) {
		fuels.put(f.getURI(), f);
	}
}
