package com.billkuker.rocketry.motorsim.io;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.fuel.FuelResolver;
import com.billkuker.rocketry.motorsim.fuel.FuelResolver.FuelNotFound;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class MotorIO {
	
	static class FuelConverter implements Converter{

		@Override
		public boolean canConvert(Class c) {
			return Fuel.class.isAssignableFrom(c);
		}

		@Override
		public void marshal(Object o, HierarchicalStreamWriter w,
				MarshallingContext ctx) {
			Fuel f = (Fuel)o;
			w.setValue(f.getURI().toString());
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader r,
				UnmarshallingContext ctx) {
			String text = r.getValue();
			try {
				URI u = new URI(text);
				return FuelResolver.getFuel(u);
			} catch (URISyntaxException e) {
				throw new Error("Bad Fuel URI: " + text, e);
			} catch (FuelNotFound e) {
				throw new Error("Can't find fuel: " + text, e);
			}
		}
		
	}
	
	static class AmountConverter implements Converter{
		
		@SuppressWarnings("unchecked")
		public void marshal(Object o, HierarchicalStreamWriter w, MarshallingContext c) {
			Amount a = (Amount)o;
			String text;
			//Leave off the fractional part if it is not relevant so we get exact values back
			if (a.isExact())
				text = a.getExactValue() + " " + a.getUnit();
			else
				text = a.doubleValue(a.getUnit()) + " " + a.getUnit();
			w.setValue(text);
		}

		public Object unmarshal(HierarchicalStreamReader r, UnmarshallingContext c) {
			String text = r.getValue();
			return Amount.valueOf(text);
		}

		public boolean canConvert(Class c) {
			return c.equals(Amount.class);
		}
		
	
	}

	
	@SuppressWarnings("deprecation")
	private static XStream getXStream(){
		XStream xstream = new XStream();
		xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
		xstream.registerConverter(new AmountConverter());
		xstream.registerConverter(new FuelConverter());
		xstream.registerConverter(new JavaBeanConverter(xstream.getClassMapper(), "class"), -20); 
		return xstream;
	}
	
	public static void writeMotor(Motor m, File f) throws IOException{
		FileWriter fout = new FileWriter(f);
		ObjectOutputStream out = getXStream().createObjectOutputStream(fout);
		out.writeObject(m);
		out.close();
		fout.close();
	}
	
	public static Motor readMotor(File f) throws IOException{
		FileReader fin = new FileReader(f);
		ObjectInputStream in = getXStream().createObjectInputStream(fin);
		Motor m;
		try {
			m = (Motor)in.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException("Class not found", e);
		}
		return m;
	}
	
	public static String writeMotor(Motor m) throws IOException{
		StringWriter sout = new StringWriter();
		ObjectOutputStream out = getXStream().createObjectOutputStream(sout);
		out.writeObject(m);
		out.close();
		sout.close();
		return sout.toString();
	}
	
	public static Motor readMotor(String s) throws IOException{
		StringReader sin = new StringReader(s);
		ObjectInputStream in = getXStream().createObjectInputStream(sin);
		Motor m;
		try {
			m = (Motor)in.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException("Class not found", e);
		}
		return m;
	}
}
