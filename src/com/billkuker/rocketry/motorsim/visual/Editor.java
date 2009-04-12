package com.billkuker.rocketry.motorsim.visual;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.sun.media.sound.Toolkit;

public class Editor extends JPanel {
	private static Logger log = Logger.getLogger(Editor.class);

	private Object obj;

	public Editor(Object o) throws IntrospectionException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		obj = o;
		/*
		BeanInfo b = Introspector.getBeanInfo(o.getClass());
		setLayout(new GridLayout(b.getPropertyDescriptors().length - 1, 2));
		for (PropertyDescriptor p : b.getPropertyDescriptors()) {
			log.debug(p.getName());
			if (p.getName().equals("class"))
				continue;

			add(new JLabel(p.getName()));
			if (p.getPropertyType().isAssignableFrom(Amount.class)){
				add(new AmountEditor(p));
			} else if ( p.getPropertyType() == boolean.class ){
				add( new BooleanEditor(p) );
				
			}
		}
		log.debug(PropertyEditorManager.findEditor(boolean.class).supportsCustomEditor());*/
		
		PropertyEditorManager.registerEditor(Amount.class, AmountPropertyEditor.class);
		
		final PropertySheetPanel ps = new PropertySheetPanel();
		ps.setBeanInfo(Introspector.getBeanInfo(obj.getClass()));
		ps.readFromObject(obj);
		add(ps);
		
		ps.addPropertySheetChangeListener(new PropertyChangeListener(){

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				//When something changes just update the
				//object, I want the changes to be immediate.
				try {
					ps.writeToObject(obj);
				} catch ( Exception v ){
					//TODO
					v.printStackTrace();
					java.awt.Toolkit.getDefaultToolkit().beep();
				} finally {
					ps.readFromObject(obj);
				}
			}
			
		});
	}

	public static void main(String args[]) throws Exception {
		CoredCylindricalGrain g = new CoredCylindricalGrain();
		g.setOD(Amount.valueOf(1, NonSI.INCH));
		new Editor(g).show();
		
		new GrainPanel(g).show();
	}

	public void show() {
		JFrame f = new JFrame();
		f.setSize(600,400);
		f.setContentPane(this);
		f.setDefaultCloseOperation(f.DISPOSE_ON_CLOSE);
		f.setVisible(true);
	}
	
	public static class AmountPropertyEditor extends PropertyEditorSupport{
		JTextField editor = new JTextField();
		Unit oldUnit;

		@Override
		public boolean supportsCustomEditor() {
			return true;
		}
		@Override public String getAsText(){
			return editor.getText();
		}
		
		@Override public Object getValue(){
			String text = editor.getText().trim();
			try {
				try {
					return Amount.valueOf(Integer.parseInt(text), oldUnit);
				} catch (NumberFormatException e){
					
				}
				Amount a = Amount.valueOf(Double.parseDouble(text), oldUnit);
				return a;
			} catch (NumberFormatException e){
				Amount a = Amount.valueOf(editor.getText());
				oldUnit = a.getUnit();
				return a;
			}

		}
		@Override public void setValue(Object o){
			Amount a = (Amount)o;
			oldUnit = a.getUnit();
			
			String text;
			if ( a.isExact() )
					 text = a.getExactValue() + " " + a.getUnit();
			else
				 text = a.doubleValue(a.getUnit()) + " " + a.getUnit();
			
			setAsText(text);
		}
		@Override public void setAsText(String text) throws IllegalArgumentException {
			editor.setText(text);
		};
		@Override
		public Component getCustomEditor(){
			return editor;
		}
	}



}
