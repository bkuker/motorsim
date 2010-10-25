package com.billkuker.rocketry.motorsim.visual;

import java.awt.Component;
import java.awt.Dimension;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;

import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.CylindricalChamber;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class Editor extends PropertySheetPanel {
	private static final long serialVersionUID = 1L;
	private static final NumberFormat nf = new DecimalFormat("##########.###");
	private static final Logger log = Logger.getLogger(Editor.class);

	private Object obj;

	@SuppressWarnings("deprecation")
	public Editor(Object o) {
		obj = o;

		PropertyEditorManager.registerEditor(Amount.class,
				AmountPropertyEditor.class);
		
		setToolBarVisible(false);
		//setMinimumSize(new Dimension(150,200));
		
		getRendererRegistry().registerRenderer(Amount.class, AmountRenderer.class);

		// Build the list of properties we want it to edit
		//final PropertySheetPanel ps = new PropertySheetPanel();
		PropertyDescriptor props[];
		try {
			props = Introspector.getBeanInfo(obj.getClass())
					.getPropertyDescriptors();
		} catch (IntrospectionException e) {
			throw new Error(e);
		}
		Vector<PropertyDescriptor> v = new Vector<PropertyDescriptor>();
		for (int i = 0; i < props.length; i++) {
			if (props[i].getName().equals("class"))
				continue;
			v.add(props[i]);
		}
		setProperties(v.toArray(new PropertyDescriptor[v.size()]));

		readFromObject(obj);
		
		getTable().setRowHeight(22);
		
		setMinimumSize(new Dimension(
				getTable().getPreferredSize().width,
				getTable().getPreferredSize().height + 10));

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

	public void showAsWindow() {
		JFrame f = new JFrame();
		f.setTitle(obj.getClass().getName());
		f.setSize(600, 400);
		f.setContentPane(this);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setVisible(true);
	}
	
	public static void main(String args[]){
		CylindricalChamber o = new CylindricalChamber();
		o.setLength(Amount.valueOf(100.5, SI.MILLIMETER));
		o.setID(Amount.valueOf(30, SI.MILLIMETER));
		Editor e = new Editor(o);
		e.showAsWindow();
	}
	
	public static class AmountRenderer implements TableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Amount a = (Amount)value;
			return new JLabel(nf.format(a.doubleValue(a.getUnit())) + " " + a.getUnit() );
		}
		
	}

	public static class AmountPropertyEditor extends PropertyEditorSupport {
		JTextField editor = new JTextField();
		Unit<?> oldUnit;

		@Override
		public boolean supportsCustomEditor() {
			return true;
		}

		@Override
		public String getAsText() {
			return editor.getText();
		}

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
				Amount<?> a = Amount.valueOf(Double.parseDouble(text), oldUnit);
				return a;
			} catch (NumberFormatException e) {
				// Storing the old unit allows you to type 10 into a field
				// that says 20 mm and get 10 mm, so you dont have to
				// type the unit if they havn't changed.
				
				//Amount wants a leading 0
				if (text.startsWith(".")){
					text = "0" + text;
				}
				
				Amount<?> a = Amount.valueOf(text);
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
				text = nf.format(a.doubleValue(a.getUnit())) + " " + a.getUnit();

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
