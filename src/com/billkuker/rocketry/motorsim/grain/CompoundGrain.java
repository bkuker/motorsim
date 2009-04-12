package com.billkuker.rocketry.motorsim.grain;

import java.awt.geom.Area;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.MotorPart;

public class CompoundGrain extends MotorPart implements Grain {
	
	private Set<Grain> grains = new HashSet<Grain>();
	
	public CompoundGrain(){

	}
	
	public void add( Grain g ){
		grains.add(g);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		for ( Grain g : grains )
			if ( g instanceof MotorPart )
				((MotorPart)g).addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		for ( Grain g : grains )
			if ( g instanceof MotorPart )
				((MotorPart)g).removePropertyChangeListener(listener);
	}

	public Area getCrossSection(Amount<Length> regression) {
		Area a = new Area();
		for ( Grain g : grains )
			a.add( g.getCrossSection(regression) );
		return a;
	}

	public Area getSideView(Amount<Length> regression) {
		Area a = new Area();
		for ( Grain g : grains )
			a.add( g.getSideView(regression) );
		return a;
	}

	public Amount<javax.measure.quantity.Area> surfaceArea(
			Amount<Length> regression) {
		Amount<javax.measure.quantity.Area> a = Amount.valueOf(0, SI.SQUARE_METRE);
		for ( Grain g : grains )
			a = a.plus(g.surfaceArea(regression));
		return a;
	}

	public Amount<Volume> volume(Amount<Length> regression) {
		Amount<Volume> v = Amount.valueOf(0, SI.CUBIC_METRE);
		for ( Grain g : grains )
			v = v.plus(g.volume(regression));
		return v;
	}

	public Amount<Length> webThickness() {
		Amount<Length> l = Amount.valueOf(0, SI.MILLIMETER);
		for ( Grain g : grains ){
			Amount<Length> gl = g.webThickness();
			if ( gl.isGreaterThan(l) )
				l = gl;
		}
		return l;
	}

}
