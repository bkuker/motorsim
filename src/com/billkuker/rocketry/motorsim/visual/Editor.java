package com.billkuker.rocketry.motorsim.visual;

import java.awt.Component;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import javax.measure.unit.Unit;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;

import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class Editor extends PropertySheetPanel {
	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(Editor.class);

	private Object obj;

	public Editor(Object o) throws IntrospectionException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		obj = o;

		PropertyEditorManager.registerEditor(Amount.class,
				AmountPropertyEditor.class);

		// Build the list of properties we want it to edit
		//final PropertySheetPanel ps = new PropertySheetPanel();
		PropertyDescriptor props[] = Introspector.getBeanInfo(obj.getClass())
				.getPropertyDescriptors();
		Vector<PropertyDescriptor> v = new Vector<PropertyDescriptor>();
		for (int i = 0; i < props.length; i++) {
			if (props[i].getName().equals("class"))
				continue;
			v.add(props[i]);
		}
		setProperties(v.toArray(new PropertyDescriptor[v.size()]));

		readFromObject(obj);

		addPropertySheetChangeListener(new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				// When something changes just update the
				// object, I want the changes to be immediate.
				try {
					log.debug("Writing properties to object.");
					writeToObject(obj);
				} catch (Exception v) {
					// TODO
					v.printStackTrace();
					java.awt.Toolkit.getDefaultToolkit().beep();
				} finally {
					readFromObject(obj);
				}
			}

		});
	}

	public void show() {
		JFrame f = new JFrame();
		f.setTitle(obj.getClass().getName());
		f.setSize(600, 400);
		f.setContentPane(this);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setVisible(true);
	}

	public static class AmountPropertyEditor extends PropertyEditorSupport {
		JTextField editor = new JTextField();
		Unit oldUnit;

		@Override
		public boolean supportsCustomEditor() {
			return true;
		}

		@Override
		public String getAsText() {
			return editor.getText();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object getValue() {
			String text = editor.getText().trim();

			// Trying to determine if the value is integer or
			// has a decimal part will prevent the uncertainty
			// term from appearing when user types an exact value
			try {
				try {
					return Amount.valueOf(Integer.parseInt(text), oldUnit);
				} catch (NumberFormatException e) {

				}
				Amount a = Amount.valueOf(Double.parseDouble(text), oldUnit);
				return a;
			} catch (NumberFormatException e) {
				// Storing the old unit allows you to type 10 into a field
				// that says 20 mm and get 10 mm, so you dont have to
				// type the unit if they havn't changed.
				Amount a = Amount.valueOf(editor.getText());
				oldUnit = a.getUnit();
				return a;
			}

		}

		@SuppressWarnings("unchecked")
		@Override
		public void setValue(Object o) {
			Amount a = (Amount) o;
			oldUnit = a.getUnit();

			String text;
			//Leave off the fractional part if it is not relevant
			if (a.isExact())
				text = a.getExactValue() + " " + a.getUnit();
			else
				text = a.doubleValue(a.getUnit()) + " " + a.getUnit();

			setAsText(text);
		}

		@Override
		public void setAsText(String text) throws IllegalArgumentException {
			editor.setText(text);
		};

		@Override
		public Component getCustomEditor() {
			return editor;
		}
	}

}
