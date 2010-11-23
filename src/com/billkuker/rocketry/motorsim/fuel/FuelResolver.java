package com.billkuker.rocketry.motorsim.fuel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.io.MotorIO;

public class FuelResolver {
	public static class FuelNotFound extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	public static interface FuelsChangeListener {
		public void fuelsChanged();
	}

	private static Set<FuelsChangeListener> listeners = new HashSet<FuelResolver.FuelsChangeListener>();
	private static Map<URI, Fuel> fuels = new HashMap<URI, Fuel>();
	private static Map<Fuel, URI> uris = new HashMap<Fuel, URI>();

	static {
		try {
			add(new KNSB(), new URI("motorsim:KNSB"));
			add(new KNDX(), new URI("motorsim:KNDX"));
			add(new KNSU(), new URI("motorsim:KNSU"));
			add(new KNER(), new URI("motorsim:KNER"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public static void addFuelsChangeListener(FuelsChangeListener l){
		listeners.add(l);
	}
	
	public static void removeFuelsChangeListener(FuelsChangeListener l){
		listeners.remove(l);
	}
	
	public static Map<URI, Fuel> getFuelMap(){
		return Collections.unmodifiableMap(fuels);
	}

	public static URI getURI( Fuel f ){
		return uris.get(f);
	}
	
	public static Fuel getFuel(URI u) throws FuelNotFound {
		if (fuels.containsKey(u))
			return fuels.get(u);
		return tryResolve(u);
	}

	private static Fuel tryResolve(URI u) throws FuelNotFound {
		File f = new File(u);
		try {
			Fuel fuel = MotorIO.readFuel(new FileInputStream(f));
			add(fuel, u);
			return fuel;
		} catch (FileNotFoundException e) {
			throw new FuelNotFound();
		} catch (IOException e) {
			throw new FuelNotFound();
		}
	}

	public static void add(Fuel f, URI uri) {
		fuels.put(uri, f);
		uris.put(f, uri);
		for ( FuelsChangeListener l : listeners )
			l.fuelsChanged();
	}
}
