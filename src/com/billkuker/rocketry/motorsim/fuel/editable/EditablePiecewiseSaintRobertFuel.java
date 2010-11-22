package com.billkuker.rocketry.motorsim.fuel.editable;

import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.VolumetricDensity;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.fuel.EditableCombustionProduct;
import com.billkuker.rocketry.motorsim.fuel.PiecewiseSaintRobertFuel;

public class EditablePiecewiseSaintRobertFuel extends PiecewiseSaintRobertFuel implements EditableFuel {

	@SuppressWarnings("unchecked")
	private Amount<VolumetricDensity> idealDensity = (Amount<VolumetricDensity>) Amount
			.valueOf("1 g/mm^3");
	
	private double combustionEfficiency = 1;
	private double densityRatio = 1;
	private EditableCombustionProduct cp;
	private String name = "New Fuel";

	public EditablePiecewiseSaintRobertFuel() {
		super(Type.SI);
		clear();
		cp = new EditableCombustionProduct();
	}
	
	private Map<Amount<Pressure>, Double> aMap;
	private Map<Amount<Pressure>, Double> nMap;
	
	public void clear(){
		super.clear();
		aMap = new HashMap<Amount<Pressure>, Double>();
		nMap = new HashMap<Amount<Pressure>, Double>();
	}
	
	public void setType(Type t){
		super.setType(t);
	}

	public void add(Amount<Pressure> p, final double _a, final double _n) {
		super.add(p, _a, _n);
		aMap.put(p, _a);
		nMap.put(p, _n);
	}
	
	public Amount<VolumetricDensity> getIdealDensity() {
		return idealDensity;
	}

	public void setIdealDensity(Amount<VolumetricDensity> idealDensity) {
		this.idealDensity = idealDensity;
	}

	public double getCombustionEfficiency() {
		return combustionEfficiency;
	}

	public void setCombustionEfficiency(double combustionEfficiency) {
		this.combustionEfficiency = combustionEfficiency;
	}

	public double getDensityRatio() {
		return densityRatio;
	}

	public void setDensityRatio(double densityRatio) {
		this.densityRatio = densityRatio;
	}

	@Override
	public CombustionProduct getCombustionProduct() {
		return cp;
	}
	
	public void setCombustionProduct(final CombustionProduct product){
		this.cp = (EditableCombustionProduct)product;
	}

	@Override
	public String toString(){
		return getName();
	}
	
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<Amount<Pressure>, Double> getAMap() {
		return aMap;
	}

	public void setAMap(Map<Amount<Pressure>, Double> aMap) {
		this.aMap = aMap;
		reset();
	}

	public Map<Amount<Pressure>, Double> getNMap() {
		return nMap;
	}

	public void setNMap(Map<Amount<Pressure>, Double> nMap) {
		this.nMap = nMap;
		reset();
	}

	private void reset(){
		if (nMap != null && aMap != null && nMap.size() == aMap.size()){
			for ( Amount<Pressure> p : aMap.keySet() ){
				add(p, aMap.get(p), nMap.get(p));
			}
		}
	}
}