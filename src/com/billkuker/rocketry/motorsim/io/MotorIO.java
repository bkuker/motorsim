package com.billkuker.rocketry.motorsim.io;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.MotorPart;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class MotorIO {
	static class AmountConverter implements Converter{
		
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
	
	private static XStream getXStream(){
		XStream xstream = new XStream();
		xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
		xstream.omitField(MotorPart.class, "pcs");
		xstream.omitField(MotorPart.class, "vcs");
		xstream.registerConverter(new AmountConverter());
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
		ObjectInputStream in = getXStream().createObjectInputStream(new FileReader(f));
		Motor m;
		try {
			m = (Motor)in.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException("Can not read motor: Class not found.", e);
		}
		return m;
	}
	
}
