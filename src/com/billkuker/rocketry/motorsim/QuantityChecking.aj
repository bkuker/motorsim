package com.billkuker.rocketry.motorsim;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

public aspect QuantityChecking {

	public interface Checked {
	};

	declare parents: Motor || Grain || Chamber || Nozzle || Fuel extends Checked;

	void around(Checked c, Amount amt):
	        execution(void Checked+.set*(Amount)) && target(c) && args(amt) {
		System.out.println(thisJoinPointStaticPart.getSignature().getName()
				+ " set to " + amt);
		try {
			BeanInfo b = Introspector.getBeanInfo(c.getClass());
			PropertyDescriptor ps[] = b.getPropertyDescriptors();
			String name = thisJoinPointStaticPart.getSignature().getName();
			name = name.replaceFirst("set", "");
			for (int i = 0; i < ps.length; i++) {
				if (ps[i].getName().equals(name)) {
					Type t = ps[i].getReadMethod().getGenericReturnType();
					ParameterizedType p = (ParameterizedType) t;
					Class expected = (Class) p.getActualTypeArguments()[0];
					Field f = expected.getDeclaredField("UNIT");
					Unit u = (Unit) f.get(null);

					Amount a = amt;
					if (!a.getUnit().isCompatible(u)) {
						System.err.println("Aspect Expected " + expected
								+ " got " + u);

						throw new Error(ps[i].getShortDescription()
								+ " must be in units of "
								+ expected.getSimpleName());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		proceed(c, amt);
	}
}
