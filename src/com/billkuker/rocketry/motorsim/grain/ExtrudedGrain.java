package com.billkuker.rocketry.motorsim.grain;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.measure.quantity.Area;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.visual.Chart;

public class ExtrudedGrain implements Grain, Grain.Graphical {
	
	Set<Shape> plus =  new HashSet<Shape>();
	Set<Shape> minus =  new HashSet<Shape>();
	Set<Shape> inhibited = new HashSet<Shape>();
	
	Amount<Length> length = Amount.valueOf(25, SI.MILLIMETER);
	
	Amount<Length> rStep;
	
	private class RegEntry{
		Amount<Area> surfaceArea;
		Amount<Volume> volume;
	}
	
	SortedMap<Amount<Length>, RegEntry> data = new TreeMap<Amount<Length>, RegEntry>();
	
	Amount<Length> webThickness;
	
	{
		/* Similar test grain*/
		Shape outside = new Ellipse2D.Double(50,50,30,30);
		plus.add(outside);
		minus.add(new Ellipse2D.Double(50,60,10,10));
		inhibited.add(outside);
		length = Amount.valueOf(70, SI.MILLIMETER);
		/*/
		
		/*Big c-slot
		Shape outside = new Ellipse2D.Double(0,0,30,30);
		plus.add(outside);
		inhibited.add(outside);
		minus.add(new Rectangle2D.Double(12,12,6,20));
		length = Amount.valueOf(70, SI.MILLIMETER);
		*/
		
		/*Plus sign
		Shape outside = new Ellipse2D.Double(0,0,200,200);
		plus.add(outside);
		inhibited.add(outside);
		minus.add(new Rectangle2D.Double(90,40,20,120));
		minus.add(new Rectangle2D.Double(40,90,120,20));
		*/
		
		findWebThickness();
		
		fillInData();
	}
	
	@Override
	public Amount<Area> surfaceArea(Amount<Length> regression) {
		if ( regression.isGreaterThan(webThickness) )
			return Amount.valueOf(0, Area.UNIT);
		Amount<Length> highKey =  data.tailMap(regression).firstKey();
		Amount<Length> lowKey;
		try{
			lowKey =  data.headMap(regression).lastKey();
		} catch ( NoSuchElementException e ){
			return data.get(highKey).surfaceArea;
		}
		
		double lp = regression.minus(lowKey).divide(highKey.minus(lowKey)).to(Dimensionless.UNIT).doubleValue(Dimensionless.UNIT);
		
		Amount<Area> lowVal = data.get(lowKey).surfaceArea;
		Amount<Area> highVal = data.get(highKey).surfaceArea;
		
		return lowVal.times(1-lp).plus(highVal.times(lp));
	}
	
	@Override
	public Amount<Volume> volume(Amount<Length> regression) {
		if ( regression.isGreaterThan(webThickness) )
			return Amount.valueOf(0, Volume.UNIT);
		Amount<Length> highKey =  data.tailMap(regression).firstKey();
		Amount<Length> lowKey;
		try{
			lowKey =  data.headMap(regression).lastKey();
		} catch ( NoSuchElementException e ){
			return data.get(highKey).volume;
		}
		
		double lp = regression.minus(lowKey).divide(highKey.minus(lowKey)).to(Dimensionless.UNIT).doubleValue(Dimensionless.UNIT);
		
		Amount<Volume> lowVal = data.get(lowKey).volume;
		Amount<Volume> highVal = data.get(highKey).volume;
		
		return lowVal.times(1-lp).plus(highVal.times(lp));
	}
	
	private void fillInData(){
		double max = webThickness().doubleValue(SI.MILLIMETER);
		double delta = max / 5;
		rStep = Amount.valueOf(delta, SI.MILLIMETER).divide(10);
		for( double r = 0; r <= max+1; r += delta){
			RegEntry e = new RegEntry();
			Amount<Length> regression = Amount.valueOf(r, SI.MILLIMETER);

			
			Amount<Length> rLen = length.minus(regression.times(2));
			if ( rLen.isLessThan(Amount.valueOf(0, SI.MILLIMETER)))
				break;
			
			System.out.println("Calculating area for regression " + regression);
			
			java.awt.geom.Area burn = getArea(regression);
			if ( burn.isEmpty() )
				break;
			burn.subtract(getArea(regression.plus(Amount.valueOf(.001, SI.MILLIMETER))));
			
			Amount<Area> xSection = crossSectionArea(getArea(regression));
			
			e.volume = xSection.times(rLen).to(Volume.UNIT);
			
			e.surfaceArea = perimeter(burn).divide(2).times(rLen).plus(xSection.times(2)).to(Area.UNIT);
			
			data.put(regression, e);

		}
		
		RegEntry e = new RegEntry();
		e.surfaceArea = Amount.valueOf(0, Area.UNIT);
		e.volume = Amount.valueOf(0, Volume.UNIT);
		data.put( webThickness(), e );
	}
	
	private Amount<Area> crossSectionArea(java.awt.geom.Area a ) {
		Rectangle r = a.getBounds();
		int cnt = 0;
		int total = 0;
		double e = .1;
		for( double x = r.getMinX(); x < r.getMaxX(); x += e){
			for( double y = r.getMinY(); y < r.getMaxY(); y += e){
				if ( a.contains(x, y) )
					cnt++;
				total++;
			}
		}
		System.out.println(cnt + " out of " + total);
		return Amount.valueOf(r.getWidth() * r.getHeight() * cnt / total, SI.MILLIMETER.pow(2)).to(Area.UNIT);

	}
	
	



	@Override
	public Amount<Length> webThickness() {
		return webThickness;
	}
	
	private Amount<Length> perimeter(java.awt.geom.Area a){
		PathIterator i = a.getPathIterator(new AffineTransform(), .001);
		double x=0, y=0;
		double len = 0;
		while ( !i.isDone() ){
			double coords[] = new double[6];
			int type = i.currentSegment(coords);
			if ( type == PathIterator.SEG_LINETO ){
				//System.out.println("Line");
				double nx = coords[0];
				double ny = coords[1];
				//System.out.println(x+","+y+ " to " + nx+"," + ny);
				len += Math.sqrt(Math.pow(x-nx, 2) + Math.pow(y-ny,2));
				x = nx;
				y = ny;
			} else if ( type == PathIterator.SEG_MOVETO ){
				//System.out.println("Move");
				x = coords[0];
				y = coords[1];
			} else {
				//System.err.println("Got " + type);
			}
			i.next();
		}
		return Amount.valueOf(len, SI.MILLIMETER);
	}
	
	private void findWebThickness(){
		java.awt.geom.Area a = getArea(Amount.valueOf(0, SI.MILLIMETER));
		Rectangle r = a.getBounds();
		double max = r.getWidth()<r.getHeight()?r.getHeight():r.getWidth(); //The max size
		double min = 0;
		double guess;
		while(true){
			guess = min + (max-min)/2; //Guess halfway through
			System.out.println("Min: " + min + " Guess: " + guess + " Max: " + max);
			a = getArea(Amount.valueOf(guess, SI.MILLIMETER));
			if ( a.isEmpty() ){
				//guess is too big
				max = guess;
			} else {
				//min is too big
				min = guess;
			}
			if ( (max-min) < .01 )
				break;
		}
		webThickness = Amount.valueOf(guess, SI.MILLIMETER);
		if ( webThickness.isGreaterThan(length.divide(2)))
			webThickness = length.divide(2);
	}
	private java.awt.geom.Area getArea(Amount<Length> regression){
		java.awt.geom.Area a = new java.awt.geom.Area();
		for ( Shape s: plus )
			a.add(new java.awt.geom.Area(regress(s, regression.doubleValue(SI.MILLIMETER), true)));
		for ( Shape s: minus )
			a.subtract(new java.awt.geom.Area(regress(s, regression.doubleValue(SI.MILLIMETER), false)));
		return a;
	}
	
	private Shape regress(Shape s, double mm, boolean plus){
		if ( inhibited.contains(s) )
			return s;
		if ( s instanceof Ellipse2D ){
			Ellipse2D e = (Ellipse2D)s;
			
			double d = plus?-2*mm:2*mm;
			
			double w = e.getWidth() + d;
			double h = e.getHeight() + d;
			double x = e.getX() - d/2;
			double y = e.getY() - d/2;
			
			return new Ellipse2D.Double(x,y,w,h);
		} else if ( s instanceof Rectangle2D ){
			Rectangle2D r = (Rectangle2D)s;
			
			double d = plus?-2*mm:2*mm;
			
			double w = r.getWidth() + d;
			double h = r.getHeight() + d;
			double x = r.getX() - d/2;
			double y = r.getY() - d/2;
			
			return new Rectangle2D.Double(x,y,w,h);
		}
		return null;
	}
	

	public static void main(String args[]) throws Exception {
		ExtrudedGrain e = new ExtrudedGrain();
			new GrainPanel(e).show();
		
	}

	@Override
	public void draw(Graphics2D g2d, Amount<Length> regression) {
		
		java.awt.geom.Area reg = getArea(regression);
		java.awt.geom.Area burn = getArea(regression);
		burn.subtract(getArea(regression.plus(Amount.valueOf(.001, SI.MILLIMETER))));
		java.awt.geom.Area noreg = getArea(Amount.valueOf(0,SI.MILLIMETER));
		
		Rectangle bounds = noreg.getBounds();
		g2d.scale(200/bounds.getWidth(), 200/bounds.getHeight());
		g2d.translate(-bounds.getX(), -bounds.getY());
		

		g2d.setStroke(new BasicStroke(0.5f));
		
		g2d.setColor(Color.GRAY);
		g2d.fill(reg);
		g2d.setColor(Color.RED);
		g2d.draw(burn);
		g2d.setColor(Color.BLACK);
		g2d.draw(noreg);
		
	}


}
