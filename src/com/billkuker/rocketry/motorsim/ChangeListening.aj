package com.billkuker.rocketry.motorsim;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;

import javax.swing.event.ChangeListener;

import com.billkuker.rocketry.motorsim.grain.MultiGrain;

public aspect ChangeListening {

	public interface Subject {
		// public void addPropertyChangeListener(PropertyChangeListener l);
	}

	private PropertyChangeSupport Subject.pcs;

	public void Subject.addPropertyChangeListener(PropertyChangeListener l) {
		//System.out.println("PCS Added");
		pcs.addPropertyChangeListener(l);
	}
	
	public void Subject.removePropertyChangeListener(PropertyChangeListener l) {
		//System.out.println("PCS Removed");
		pcs.addPropertyChangeListener(l);
	}
	
	public void Subject.firePropertyChange(PropertyChangeEvent e) {
		pcs.firePropertyChange(e);
	}

	declare parents: Motor || Grain || Chamber || Nozzle || Fuel implements Subject;

	void around(Subject s, Object newVal):
	        execution(void Subject+.set*(..)) && target(s) && args(newVal) {
		System.out.println(s);
		String name = thisJoinPointStaticPart.getSignature().getName();
		name = name.replaceFirst("set", "");
		Object old = null;
		try {
			String pre = "get";
			if (newVal instanceof Boolean)
				pre = "is";
			Method m = s.getClass().getMethod(pre + name);
			old = m.invoke(s);

		} catch (Throwable t) {
			System.err.print("Error getting old value for " + name);
		}
		proceed(s, newVal);
		if (old != newVal) {
			System.out.println(name + " changed from " + old + " to "
					+ newVal);
			s.pcs.firePropertyChange(name, old, newVal);
		}
	}

	after(Subject s) returning: this(s) && initialization(Subject.new(..)) {
		s.pcs = new PropertyChangeSupport(s);
	}
}
