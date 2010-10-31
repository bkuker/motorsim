package com.billkuker.rocketry.motorsim;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;
import javax.swing.JFrame;
import javax.swing.JSplitPane;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.grain.EndBurner;
import com.billkuker.rocketry.motorsim.visual.Chart;

public class GraphSimplifier<X extends Quantity, Y extends Quantity> {
	Method f;
	
	private static final int CHOOSE = 10;
	private static final int EVEN = 10;
	
	private class Entry {
		Amount<X> x;
		Amount<Y> y;
	}

	private class DDEntry implements Comparable<DDEntry> {
		Amount<X> x;
		Amount dd;

		@Override
		public int compareTo(DDEntry o) {
			return o.dd.compareTo(dd);
		}
	}

	private SortedMap<Amount<X>, Amount<Y>> out = new TreeMap<Amount<X>, Amount<Y>>();

	public Amount<Y> value(Amount<X> x) {
		return out.get(x);
	}

	public Iterable<Amount<X>> getDomain() {
		return out.keySet();
	}

	public GraphSimplifier(Object source, String method,
			Iterator<Amount<X>> domain) throws NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		f = source.getClass().getMethod(method, Amount.class);

		Vector<Entry> oldEntries = new Vector<Entry>();
		Entry max = null;
		while (domain.hasNext()) {
			Amount<X> x = domain.next();
			Amount<Y> y = (Amount<Y>) f.invoke(source, x);
			Entry e = new Entry();
			e.x = x;
			e.y = y;
			oldEntries.add(e);
			if ( max == null || e.y.isGreaterThan(max.y) )
				max = e;
		}

		List<DDEntry> byDD = new Vector<DDEntry>();
		Map<Amount<X>, Amount<Y>> byX = new HashMap<Amount<X>, Amount<Y>>();

		for (int i = 1; i < oldEntries.size() - 1; i++) {
			Entry low = oldEntries.elementAt(i - 1);
			Entry middle = oldEntries.elementAt(i);
			Entry high = oldEntries.elementAt(i + 1);
			
			//if this is one of the N even stepped
			//samples include it.
			if ( i % (oldEntries.size()/EVEN) == 0 ){
				out.put(middle.x, middle.y);
			}

			Amount d1, d2, dd;

			d1 = middle.y.minus(low.y).divide(middle.x.minus(low.x));
			d2 = high.y.minus(middle.y).divide(high.x.minus(middle.x));
			
			//dd = (d1.isGreaterThan(d2))?d1:d2;
			
			
			dd = d2.minus(d1).divide(high.x.minus(low.x));

			DDEntry dde = new DDEntry();
			dde.dd = dd.abs();
			dde.x = middle.x;

			byDD.add(dde);
			byX.put(middle.x, middle.y);

		}

		Collections.sort(byDD);

		//always include the first, MAX and last
		out.put(oldEntries.elementAt(0).x, oldEntries.elementAt(0).y);
		out.put(max.x, max.y);
		int last = oldEntries.size() - 1;
		out.put(oldEntries.elementAt(last).x, oldEntries.elementAt(last).y);
		
		int count = 0;
		for (DDEntry dde : byDD) {
			if ( out.containsKey(dde.x) )
				continue;
			out.put(dde.x, byX.get(dde.x));
			if (++count >= CHOOSE)
				break;
		}
		
		

	}

	public static void main(String args[]) throws Exception {
		/*
		CoredCylindricalGrain g = new CoredCylindricalGrain();
		g.setLength(Amount.valueOf(70, SI.MILLIMETER));
		g.setOD(Amount.valueOf(30, SI.MILLIMETER));
		g.setID(Amount.valueOf(10, SI.MILLIMETER));
		*/
		EndBurner g = new EndBurner();
		g.setLength(Amount.valueOf(70, SI.MILLIMETER));
		g.setoD(Amount.valueOf(30, SI.MILLIMETER));
		g.setPuntDepth(Amount.valueOf(10, SI.MILLIMETER));
		g.setPuntDiameter(Amount.valueOf(10, SI.MILLIMETER));
		
		JFrame f = new JFrame();
		f.setSize(1024, 768);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		f.add(jsp);

		Chart<Length, Area> c = new Chart<Length, Area>(SI.MILLIMETER,
				SI.MILLIMETER.pow(2).asType(Area.class), g, "surfaceArea");
		c.setDomain(c.new IntervalDomain(Amount.valueOf(0, SI.CENTIMETER), g
				.webThickness()));
		jsp.setTopComponent(c);

		GraphSimplifier<Length, Area> gs = new GraphSimplifier(g,
				"surfaceArea", c.new IntervalDomain(Amount.valueOf(0,
						SI.CENTIMETER), g.webThickness()).iterator());

		Chart<Length, Area> d = new Chart<Length, Area>(SI.MILLIMETER,
				SI.MILLIMETER.pow(2).asType(Area.class), gs, "value");
		d.setDomain(gs.getDomain());
		jsp.setBottomComponent(d);
		

		
		f.setVisible(true);
		jsp.setDividerLocation(.5);
		jsp.revalidate();
	}
}
