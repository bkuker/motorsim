package com.billkuker.rocketry.motorsim.visual;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.RocketScience;
import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;

public class Chart<X extends Quantity, Y extends Quantity> extends JPanel implements RocketScience.UnitPreferenceListener {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(Chart.class);
	
	private final Stroke dashed = new BasicStroke(1, 1, 1, 1, new float[]{2,4}, 0);
	private final Font labelFont = new Font(Font.DIALOG, Font.BOLD, 10);

	private static ThreadFactory fastTF = new ThreadFactory() {
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			t.setName("Fast Chart Draw");
			return t;
		}
	};
	private static ThreadFactory slowTF = new ThreadFactory() {
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			t.setName("Slow Chart Draw");
			return t;
		}
	};
	private static ExecutorService fast = Executors.newFixedThreadPool(2, fastTF);
	private static ExecutorService slow = Executors.newFixedThreadPool(2, slowTF);


	public class IntervalDomain implements Iterable<Amount<X>> {

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
			return new Iterator<Amount<X>>() {
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
					throw new UnsupportedOperationException(
							"Chart domain iterators are not modifiable.");
				}
			};
		}

	}

	XYSeriesCollection dataset = new XYSeriesCollection();
	JFreeChart chart;

	Unit<X> xUnit;
	Unit<Y> yUnit;

	Object source;
	Method f;
	
	Iterable<Amount<X>> domain;

	public Chart(Unit<X> xUnit, Unit<Y> yUnit, Object source, String method)
			throws NoSuchMethodException {
		super(new BorderLayout());
		f = source.getClass().getMethod(method, Amount.class);

		this.source = source;
		
		this.xUnit = xUnit;
		this.yUnit = yUnit;

		RocketScience.addUnitPreferenceListener(this);
		
		setup();
	}
	
	private void setup(){
		removeAll();
		this.xUnit = RocketScience.UnitPreference.getUnitPreference()
				.getPreferredUnit(xUnit);
		this.yUnit = RocketScience.UnitPreference.getUnitPreference()
				.getPreferredUnit(yUnit);

		chart = ChartFactory.createXYLineChart(f.getName().substring(0, 1)
				.toUpperCase()
				+ f.getName().substring(1), // Title
				this.xUnit.toString(), // x-axis Label
				this.yUnit.toString(), // y-axis Label
				dataset, PlotOrientation.VERTICAL, // Plot Orientation
				false, // Show Legend
				true, // Use tool tips
				false // Configure chart to generate URLs?
				);
		add(new ChartPanel(chart));
	}
	

	@Override
	public void preferredUnitsChanged() {
		setup();
		setDomain(domain);
	}
	
	
	
	public void addDomainMarker(Amount<X> x, String label, Color c){
		double xVal = x.doubleValue(xUnit);
		Marker marker = new ValueMarker(xVal);
		marker.setStroke(dashed);
		marker.setPaint(c);
		marker.setLabelPaint(c);
		marker.setLabelFont(labelFont);
		marker.setLabel(label + ": " + RocketScience.ammountToRoundedString(x));
		marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
		marker.setLabelOffset(new RectangleInsets(0, -5, 0, 0));
		chart.getXYPlot().addDomainMarker(marker);
	}
	
	public void addRangeMarker(Amount<Y> y, String label, Color c){
		double yVal = y.doubleValue(yUnit);
		Marker marker = new ValueMarker(yVal);
		marker.setStroke(dashed);
		marker.setPaint(c);
		marker.setLabelPaint(c);
		marker.setLabelFont(labelFont);
		marker.setLabel(label + ": " + RocketScience.ammountToRoundedString(y));
		marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
		marker.setLabelOffset(new RectangleInsets(0, 5, 0, 0));
		chart.getXYPlot().addRangeMarker(marker);
	}

	private Marker focusMarkerX, focusMarkerY;

	public void mark(Amount<X> m) {
		if (focusMarkerX != null)
			chart.getXYPlot().removeDomainMarker(focusMarkerX);
		if (focusMarkerY != null)
			chart.getXYPlot().removeRangeMarker(focusMarkerY);
		
		if (m != null) {
			focusMarkerX = new ValueMarker(m.doubleValue(xUnit));
			focusMarkerX.setPaint(Color.blue);
			focusMarkerX.setAlpha(0.8f);
			
			chart.getXYPlot().addDomainMarker(focusMarkerX);
			
			Amount<Y> val = getNear(m);
			if ( val != null ){
				focusMarkerY = new ValueMarker(val.doubleValue(yUnit));
				focusMarkerY.setPaint(Color.BLUE);
				focusMarkerY.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
				focusMarkerY.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
				focusMarkerY.setLabelPaint(Color.BLUE);
				focusMarkerY.setLabelFont(labelFont);
				focusMarkerY.setLabelOffset(new RectangleInsets(0,5,0,0));
				chart.getXYPlot().addRangeMarker(focusMarkerY);
				focusMarkerY.setLabel(RocketScience.ammountToRoundedString(val));
			}
		}
	}
	
	/**
	 * Get the Y value at or near a given X
	 * For display use only!
	 * 
	 * @param ax
	 * @return
	 */
	private Amount<Y> getNear(final Amount<X> ax){
		if ( dataset.getSeriesCount() != 1 )
			return null;
		final XYSeries s = dataset.getSeries(0);
		final double x = ax.doubleValue(xUnit);
		int idx = s.getItemCount() / 2;
		int delta = s.getItemCount() / 4;
		while(true){
			if ( s.getX(idx).doubleValue() < x ){
				idx += delta;
			} else {
				idx -= delta;
			}
			delta = delta / 2;
			if ( delta < 1 ){
				int idxL = idx-1;
				int idxH = idx;
				final double lowerX = s.getX(idxL).doubleValue();
				final double higherX = s.getX(idxH).doubleValue();
				final double sampleXDiff = higherX - lowerX;
				final double xDiff = x - lowerX;
				final double dist = xDiff / sampleXDiff;
				final double lowerY = s.getY(idxL).doubleValue();
				final double higherY = s.getY(idxH).doubleValue();
				final double y = lowerY + dist * (higherY - lowerY);
				
				return Amount.valueOf( y, yUnit);
			}
		}
	}
	
	private void drawDone(){
	
	}

	private volatile boolean stop = false;
	private volatile int lastSkipStepShown;
	public void setDomain(final Iterable<Amount<X>> d) {
		chart.getXYPlot().clearDomainMarkers();
		chart.getXYPlot().clearRangeMarkers();
		lastSkipStepShown = Integer.MAX_VALUE;
		stop = true;
		fill(d, 100);
		fast.submit(new Thread() {
			public void run() {
				if (!stop)
					fill(d, 10);
				slow.submit(new Thread() {
					public void run() {
						if (!stop){
							fill(d, 1);
						}
					}
				});
			}
		});
	}

	@SuppressWarnings("unchecked")
	private synchronized void fill(Iterable<Amount<X>> d, final int requestedSkip) {
		this.domain = d;
		
		log.debug(f.getName() + " " + requestedSkip + " Start");
		stop = false;
		int sz = 0;
		int calculatedSkip = requestedSkip;
		if (d instanceof Collection) {
			sz = ((Collection<Amount<X>>) d).size();
			int sk2 = sz / 200;
			if (calculatedSkip < sk2)
				calculatedSkip = sk2;
		}
		// series.clear();
		int cnt = 0;

		final XYSeries newSeries = new XYSeries(f.getName());
		try {
			Amount<X> last = null;
			for (Amount<X> ax : d) {
				if (stop) {
					log.debug(f.getName() + " " + calculatedSkip + " Abort");
					return;
				}
				last = ax;
				if (cnt % calculatedSkip == 0) {
					Amount<Y> y = (Amount<Y>) f.invoke(source, ax);
					newSeries.add(ax.doubleValue(xUnit), y.doubleValue(yUnit));
				}
				cnt++;
			}
			Amount<Y> y = (Amount<Y>) f.invoke(source, last);
			newSeries.add(last.doubleValue(xUnit), y.doubleValue(yUnit));
			SwingUtilities.invokeLater(new Thread() {
				@Override
				public void run() {
					if ( requestedSkip < lastSkipStepShown ){
						lastSkipStepShown = requestedSkip;
						dataset.removeAllSeries();
						dataset.addSeries(newSeries);
						log.debug(f.getName() + " Replaced with " + requestedSkip);
					}
					if ( requestedSkip == 1 ){
						drawDone();
					}
				}
			});
		} catch (IllegalArgumentException e) {
			log.error(e);
		} catch (IllegalAccessException e) {
			log.error(e);
		} catch (InvocationTargetException e) {
			log.error(e);
		}
		log.debug(f.getName() + " " + calculatedSkip + " Done");
	}

	public void show() {
		new JFrame() {
			private static final long serialVersionUID = 1L;
			{
				setContentPane(Chart.this);
				setSize(640, 480);
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
