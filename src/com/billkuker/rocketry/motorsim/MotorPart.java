package com.billkuker.rocketry.motorsim;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.measure.unit.Unit;

import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;

public class MotorPart {
	PropertyChangeSupport pcs;
	VetoableChangeSupport vcs;
	
	private static Logger log = Logger.getLogger(MotorPart.class);
	
	public interface Validating {
		 void checkValidity() throws ValidationException;
		 
		 public class ValidationException extends Exception {
				private static final long serialVersionUID = 1L;
				public ValidationException(Validating v, String error) {
					super(v.toString() + ": " + error);
				}
			}
	}
	
	public MotorPart() {
		pcs = new PropertyChangeSupport(this);
		vcs = new VetoableChangeSupport(this);
		
		vcs.addVetoableChangeListener(new VetoableChangeListener(){
			@SuppressWarnings("unchecked")
			public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
				if ( evt.getNewValue() instanceof Amount ){
					try {
						BeanInfo b = Introspector.getBeanInfo(evt.getSource().getClass());
						PropertyDescriptor ps[] = b.getPropertyDescriptors();
						for ( int i = 0; i < ps.length; i++ ){
							if (ps[i].getName().equals(evt.getPropertyName())){
								Type t = ps[i].getReadMethod().getGenericReturnType();
								ParameterizedType p = (ParameterizedType) t;
								Class expected = (Class)p.getActualTypeArguments()[0];
								Field f = expected.getDeclaredField("UNIT");
								Unit u = (Unit) f.get(null);
								
								Amount a = (Amount)evt.getNewValue();
								if (!a.getUnit().isCompatible(u))
									throw new PropertyVetoException(ps[i].getShortDescription()
											+ " must be in units of "
											+ expected.getSimpleName(), evt);
	
								log.debug("Expected " + expected + " got " + u);
							}
						}
					} catch ( PropertyVetoException e ){
						throw e;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	protected void firePropertyChange(PropertyChangeEvent evt) {
		pcs.firePropertyChange(evt);
	}

	protected void firePropertyChange(String propertyName, boolean oldValue,
			boolean newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	protected void firePropertyChange(String propertyName, int oldValue,
			int newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void fireVetoableChange(String arg0, boolean arg1, boolean arg2)
			throws PropertyVetoException {
		vcs.fireVetoableChange(arg0, arg1, arg2);
	}

	public void fireVetoableChange(String arg0, int arg1, int arg2)
			throws PropertyVetoException {
		vcs.fireVetoableChange(arg0, arg1, arg2);
	}

	public void fireVetoableChange(String arg0, Object arg1, Object arg2)
			throws PropertyVetoException {
		vcs.fireVetoableChange(arg0, arg1, arg2);
	}
}
