package com.billkuker.rocketry.motorsim;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

public aspect ChangeListening {

	private static Logger log = Logger.getLogger(ChangeListening.class);
	public interface Subject {
		// public void addPropertyChangeListener(PropertyChangeListener l);
	}

	private PropertyChangeSupport Subject.pcs;

	public void Subject.addPropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}
	
	public void Subject.removePropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}
	
	public void Subject.firePropertyChange(PropertyChangeEvent e) {
		pcs.firePropertyChange(e);
	}

	declare parents: Motor || Grain || Chamber || Nozzle || Fuel implements Subject;

	void around(Subject s, Object newVal):
	        execution(void Subject+.set*(..)) && target(s) && args(newVal) {
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
			log.warn("Error getting old value for " + name);
		}
		proceed(s, newVal);
		if (old != newVal) {
			s.pcs.firePropertyChange(name, old, newVal);
		}
	}

	after(Subject s) returning: this(s) && initialization(Subject.new(..)) {
		s.pcs = new PropertyChangeSupport(s);
	}
}
