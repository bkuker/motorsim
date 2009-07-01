package com.billkuker.rocketry.motorsim.visual;
import java.awt.BorderLayout;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.RocketScience;
import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;

public class Chart<X extends Quantity, Y extends Quantity> extends JPanel  {
	private static final long serialVersionUID = 1L;
	
	private static ExecutorService fast = Executors.newFixedThreadPool(2) ;
	private static ExecutorService slow = Executors.newFixedThreadPool(2);
	private boolean stop = false;

	public class IntervalDomain implements Iterable<Amount<X>>{
		
		Amount<X> low, high, delta;
		int steps = 100;

		public IntervalDomain(Amount<X> low, Amount<X> high) {
			this.low = low;
			this.high = high;
			delta = high.minus(low).divide(steps);
		}

		public IntervalDomain(Amount<X> low, Amount<X> high, int steps) {
			this.steps = steps;
			this.low = low;
			this.high = high;
			delta = high.minus(low).divide(steps);
		}


		public Iterator<Amount<X>> iterator() {
			return new Iterator<Amount<X>>(){
				Amount<X> current = low;
				
				public boolean hasNext() {
					return current.isLessThan(high.plus(delta));
				}

				public Amount<X> next() {
					Amount<X> ret = current;
					current = current.plus(delta);
					return ret;
				}
				
				public final void remove() {
					throw new UnsupportedOperationException("Chart domain iterators are not modifiable.");
				}
			};
		}
		
	}
	

	XYSeries series;
	XYSeriesCollection dataset = new XYSeriesCollection();
	JFreeChart chart;

	Unit<X> xUnit;
	Unit<Y> yUnit;

	Object source;
	Method f;


	@SuppressWarnings("unchecked")
	public Chart(Unit<X> xUnit, Unit<Y> yUnit, Object source, String method)
			throws NoSuchMethodException {
		super(new BorderLayout());
		f = source.getClass().getMethod(method, Amount.class);

		series = new XYSeries(f.getName());
		this.source = source;

		dataset.addSeries(series);

		this.xUnit = RocketScience.UnitPreference.preference.getPreferredUnit(xUnit);
		this.yUnit = RocketScience.UnitPreference.preference.getPreferredUnit(yUnit);
		
		chart = ChartFactory.createXYLineChart(
				method.substring(0,1).toUpperCase() + method.substring(1), // Title
				this.xUnit.toString(), // x-axis Label
				this.yUnit.toString(), // y-axis Label
				dataset,
				PlotOrientation.VERTICAL, // Plot Orientation
				false, // Show Legend
				true, // Use tool tips
				false // Configure chart to generate URLs?
				);
		add(new ChartPanel(chart));
	}
	
	private Marker marker;
	public void mark(Amount<X> m){
		if ( marker != null )
			chart.getXYPlot().removeDomainMarker(marker);
		if ( m != null ){
	       marker = new ValueMarker(m.doubleValue(xUnit));
	       marker.setPaint(Color.blue);
	       marker.setAlpha(0.8f);
			chart.getXYPlot().addDomainMarker(marker);
		}
	}


	public void setDomain(final Iterable<Amount<X>> d) {
		series.clear();
		stop = true;
		fill(d, 100);
		fast.submit(new Thread(){
			public void run(){
				if ( !stop )
					fill(d, 10);
				slow.submit(new Thread(){
					public void run(){
						if ( !stop )
							fill(d, 1);
					}
				});
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private synchronized void fill(Iterable<Amount<X>> d, int skip) {
		stop = false;
		int sz = 0;
		if (d instanceof Collection) {
			sz = ((Collection) d).size();
			int sk2 = sz / 200;
			if (skip < sk2)
				skip = sk2;
		}
		// series.clear();
		int cnt = 0;

		try {
			Amount<X> last = null;
			for (Amount<X> ax : d) {
				if ( stop ){
					System.out.println("Stopping early " + skip);
					return;
				}
				last = ax;
				if (cnt % skip == 0) {
					Amount<Y> y = (Amount<Y>) f.invoke(source, ax);
					add(ax, y);
				}
				cnt++;
			}
			Amount<Y> y = (Amount<Y>) f.invoke(source, last);
			add(last, y);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void add(Amount<X> x, Amount<Y> y) {
		series.add(x.doubleValue(xUnit), y.doubleValue(yUnit));
	}
	
	public void show(){
		new JFrame(){
			private static final long serialVersionUID = 1L;
			{
				setContentPane(Chart.this);
				setSize(640,480);
				setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			}
		}.setVisible(true);
	}

	public static void main(String args[]) throws Exception {
		CoredCylindricalGrain g = new CoredCylindricalGrain();
		g.setLength(Amount.valueOf(70, SI.MILLIMETER));
		g.setOD(Amount.valueOf(30, SI.MILLIMETER));
		g.setID(Amount.valueOf(10, SI.MILLIMETER));

		Chart<Length, Area> c = new Chart<Length, Area>(SI.MILLIMETER,
				SI.MILLIMETER.pow(2).asType(Area.class), g, "surfaceArea");

		c.setDomain(c.new IntervalDomain(Amount.valueOf(0, SI.CENTIMETER), g
				.webThickness()));

		c.show();
		
		Chart<Length, Volume> v = new Chart<Length, Volume>(SI.MILLIMETER,
				SI.MILLIMETER.pow(3).asType(Volume.class), g, "volume");

		v.setDomain(c.new IntervalDomain(Amount.valueOf(0, SI.CENTIMETER), g
				.webThickness()));

		v.show();
	}

}
