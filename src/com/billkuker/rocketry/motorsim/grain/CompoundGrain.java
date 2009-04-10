package com.billkuker.rocketry.motorsim.grain;

import java.awt.geom.Area;

import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Grain;

public class CompoundGrain implements Grain {
	
	private Grain a, b;
	
	public CompoundGrain(Grain a, Grain b){
		this.a = a;
		this.b = b;
	}

	public Area getCrossSection(Amount<Length> regression) {
		Area aa = a.getCrossSection(regression);
		aa.add(b.getCrossSection(regression));
		return aa;
	}

	public Area getSideView(Amount<Length> regression) {
		Area aa = a.getSideView(regression);
		aa.add(b.getSideView(regression));
		return aa;
	}

	public Amount<javax.measure.quantity.Area> surfaceArea(
			Amount<Length> regression) {
		return a.surfaceArea(regression).plus(b.surfaceArea(regression));
	}

	public Amount<Volume> volume(Amount<Length> regression) {
		return a.volume(regression).plus(b.volume(regression));
	}

	public Amount<Length> webThickness() {
		Amount<Length> l = a.webThickness();
		if ( b.webThickness().isGreaterThan(l) )
			return b.webThickness();
		return l;
	}

}
