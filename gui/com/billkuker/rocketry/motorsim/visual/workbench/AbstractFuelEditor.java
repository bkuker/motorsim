package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.Component;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.fuel.EditableCombustionProduct;
import com.billkuker.rocketry.motorsim.fuel.PiecewiseSaintRobertFuel;
import com.billkuker.rocketry.motorsim.visual.Chart;
import com.billkuker.rocketry.motorsim.visual.Editor;

public abstract class AbstractFuelEditor  extends JSplitPane {
	private static final long serialVersionUID = 1L;

	protected static class EditablePSRFuel extends PiecewiseSaintRobertFuel {

		@SuppressWarnings("unchecked")
		private Amount<VolumetricDensity> idealDensity = (Amount<VolumetricDensity>) Amount
				.valueOf("1 g/mm^3");
		
		private double combustionEfficiency = 1;
		private double densityRatio = 1;
		private EditableCombustionProduct cp;
		private String name = "New Fuel";

		public EditablePSRFuel(Type t) {
			super(t);
			cp = new EditableCombustionProduct();
		}
		
		public void clear(){
			super.clear();
		}
		
		public void setType(Type t){
			super.setType(t);
		}

		public void add(Amount<Pressure> p, final double _a, final double _n) {
			super.add(p, _a, _n);

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

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}
	
	private final JSplitPane editParent;
	private final JSplitPane editTop;
	private final Fuel f;
	private Chart<Pressure, Velocity> burnRate;
	
	public AbstractFuelEditor(Fuel f){
		super(HORIZONTAL_SPLIT);
		this.f = f;
		
		editTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		editTop.setTopComponent(new Editor(f));
		editTop.setBottomComponent(new Editor(f.getCombustionProduct()));
		
		editParent = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		setLeftComponent(editParent);
		editParent.setTopComponent(editTop);
		editParent.setBottomComponent(getBurnrateEditComponent());
		
		setResizeWeight(0);
		setDividerLocation(.3);
		editParent.setDividerLocation(.5);
		editTop.setDividerLocation(.5);
		editParent.resetToPreferredSizes();
		revalidate();

		update();
	}
	
	protected abstract Component getBurnrateEditComponent();
	
	public Fuel getFuel(){
		return f;
	}
	


	protected void update() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				editTop.setTopComponent(new Editor(f));
				editTop.setBottomComponent(new Editor(f.getCombustionProduct()));
				if (burnRate != null)
					AbstractFuelEditor.this.remove(burnRate);
				try {
					burnRate = new Chart<Pressure, Velocity>(
							SI.MEGA(SI.PASCAL), SI.MILLIMETER.divide(SI.SECOND)
									.asType(Velocity.class), f, "burnRate");
				} catch (NoSuchMethodException e) {
					throw new Error(e);
				}
				burnRate.setDomain(burnRate.new IntervalDomain(Amount.valueOf(
						0, SI.MEGA(SI.PASCAL)), Amount.valueOf(11, SI
						.MEGA(SI.PASCAL)), 50));
				AbstractFuelEditor.this.setRightComponent(burnRate);
				AbstractFuelEditor.this.revalidate();
			}
		});
	}
	
}
