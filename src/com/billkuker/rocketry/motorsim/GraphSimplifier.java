package com.billkuker.rocketry.motorsim;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;
import com.billkuker.rocketry.motorsim.visual.Chart;

public class GraphSimplifier<X extends Quantity, Y extends Quantity> {
	Method f;

	private class Entry {
		Amount<X> x;
		Amount<Y> y;
	}

	private Map<Amount<X>, Amount<Y>> out = new HashMap<Amount<X>, Amount<Y>>();
	private List<Amount<X>> outDomain = new Vector<Amount<X>>();

	public Amount<Y> value(Amount<X> x) {
		return out.get(x);
	}

	public Iterable<Amount<X>> getDomain() {
		return outDomain;
	}

	public GraphSimplifier(Object source, String method,
			Iterator<Amount<X>> domain) throws NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		f = source.getClass().getMethod(method, Amount.class);

		Vector<Entry> oldEntries = new Vector<Entry>();

		Amount<Y> max = null, min = null, range = null, err = null;
		while (domain.hasNext()) {
			Amount<X> x = domain.next();
			Amount<Y> y = (Amount<Y>) f.invoke(source, x);
			Entry e = new Entry();
			e.x = x;
			e.y = y;
			oldEntries.add(e);

			if (max == null || max.isLessThan(y))
				max = y;
			if (min == null || min.isGreaterThan(y))
				min = y;
		}
		range = max.minus(min).abs();
		err = range.divide(50);

		for (int j = 0; j < 10 && oldEntries.size() > 30; j++) {
			Vector<Entry> newEntries = new Vector<Entry>();
			newEntries.add(oldEntries.firstElement());
			for (int i = 1; i < oldEntries.size() - 1; i = i + 2) {
				Entry low = oldEntries.elementAt(i - 1);
				Entry middle = oldEntries.elementAt(i);
				Entry high = oldEntries.elementAt(i + 1);

				Amount<X> dx = high.x.minus(low.x);
				Amount<Y> dy = high.y.minus(low.y);

				Amount<X> ddx = middle.x.minus(low.x);
				Amount<Y> lin = low.y.plus(dy.times(ddx.divide(dx)));

				Amount<Y> dif = middle.y.minus(lin).abs();

				// System.out.println(middle.x + "\t" + middle.y + "\t" + lin +
				// "\t" + dif + "\t"
				// + dy);

				if (dif.isGreaterThan(err)) {
					newEntries.add(middle);
				}
				newEntries.add(high);
			}
			newEntries.add(oldEntries.lastElement());

			System.out.println("Size " + oldEntries.size() + " -> "
					+ newEntries.size());

			oldEntries = newEntries;
		}

		for (Entry ee : oldEntries) {
			if (out.get(ee.x) == null) {
				out.put(ee.x, ee.y);
				outDomain.add(ee.x);
			}
		}
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

		GraphSimplifier<Length, Area> gs = new GraphSimplifier(g,
				"surfaceArea", c.new IntervalDomain(Amount.valueOf(0,
						SI.CENTIMETER), g.webThickness()).iterator());

		Chart<Length, Area> d = new Chart<Length, Area>(SI.MILLIMETER,
				SI.MILLIMETER.pow(2).asType(Area.class), gs, "value");
		d.setDomain(gs.getDomain());
		d.show();
	}
}
