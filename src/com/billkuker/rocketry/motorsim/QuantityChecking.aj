package com.billkuker.rocketry.motorsim;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.measure.unit.Unit;

import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;

public aspect QuantityChecking {

	private static Logger log = Logger.getLogger(QuantityChecking.class);
	public interface Checked {
	};

	declare parents: Motor || Grain || Chamber || Nozzle || Fuel extends Checked;

	@SuppressWarnings("unchecked")
	void around(Checked c, Amount amt):
	        execution(void Checked+.set*(Amount)) && target(c) && args(amt) {
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
						log.warn("Aspect Expected " + expected
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
