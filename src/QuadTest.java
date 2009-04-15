import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class QuadTest extends JPanel {


	public void paint(Graphics g){
		super.paint(g);
		Graphics2D g2d = (Graphics2D)g;

		g2d.scale(30, 30);

		Area a;
		GeneralPath p;

		g2d.setStroke(new BasicStroke(.02f));
		
		
		/*
		 a = new Area(new Rectangle2D.Double(0,0,1,1));
		//a.transform(AffineTransform.getRotateInstance(Math.PI/4.0));
		g2d.setColor(Color.black);
		g2d.draw(a);
		g2d.setColor(Color.red);
		
		g2d.draw(square( a ));
		System.out.println(
				yRotatedVolume(a) + ", " +
				yRotatedSurfaceArea(a) + "\n"
				);

		
		a = new Area( new Ellipse2D.Double(2, 5, 4, 4));
		g2d.setColor(Color.black);
		g2d.draw(a);
		g2d.setColor(Color.red);
		
		g2d.draw(square( a ));
		System.out.println(
				yRotatedVolume(a) + ", " +
				yRotatedSurfaceArea(a) + "\n"
				);
		
	
		*/
		p = new GeneralPath();
		p.moveTo(0,0);
		p.lineTo(1, 1);
		p.lineTo(0,1);
		p.closePath();
		a = new Area(p);
		g2d.translate(0, 8);
		g2d.setColor(Color.black);
		g2d.draw(a);
		g2d.setColor(Color.red);
		
		g2d.draw(square( a ));
		System.out.println(
				yRotatedVolume(a) + ", " +
				yRotatedSurfaceArea(a) + "\n"
				);
		
		p = new GeneralPath();
		p.moveTo(0,0);
		p.lineTo(2, 2);
		p.lineTo(0,2);
		p.closePath();
		a = new Area(p);
		g2d.translate(0, 2);
		g2d.setColor(Color.black);
		g2d.draw(a);
		g2d.setColor(Color.red);
		
		g2d.draw(square( a ));
		System.out.println(
				yRotatedVolume(a) + ", " +
				yRotatedSurfaceArea(a) + "\n"
				);

		p = new GeneralPath();
		p.moveTo(0,1);
		p.lineTo(1,1);
		p.lineTo(2, 2);
		p.lineTo(0,2);
		p.closePath();
		a = new Area(p);
		g2d.translate(0, 2);
		g2d.setColor(Color.black);
		g2d.draw(a);
		g2d.setColor(Color.red);
		
		g2d.draw(square( a ));
		System.out.println(
				yRotatedVolume(a) + ", " +
				yRotatedSurfaceArea(a) + "\n"
				);
		 

	}
	
	
	private double yRotatedVolume(Area a){
		return Math.PI * area(new Area(square(a)));
	}
	
	private Shape square(java.awt.geom.Area a) {
		PathIterator i = a.getPathIterator(new AffineTransform(), 0.0001);
		GeneralPath cur = new GeneralPath();

		double last[] = {0,0};
		while (!i.isDone()) {
			double coords[] = new double[6];
			int type = i.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_CLOSE:
				cur.closePath();
				break;
			case PathIterator.SEG_MOVETO:
				cur.moveTo(Math.pow(coords[0],2), coords[1]);
				last[0] = coords[0];
				last[1] = coords[1];
				break;
			case PathIterator.SEG_CUBICTO:
				throw new Error("Non-flattened geometry!");
			case PathIterator.SEG_LINETO:
				double x = last[0];
				double y = last[1];
				double len = Math.sqrt(Math.pow(last[0]-coords[0], 2) + Math.pow(last[1]-coords[1], 2));
				int steps = (int)(len / .01) + 5;
				for (int s = 0; s < steps; s++) {
					x += (coords[0] - last[0]) / steps;
					y += (coords[1] - last[1]) / steps;
					cur.lineTo(Math.pow(x, 2), y);
				}
				last[0] = coords[0];
				last[1] = coords[1];
				break;
			case PathIterator.SEG_QUADTO:
				throw new Error("Non-flattened geometry!");

			}
			i.next();
		}
		return cur;
	}
	
	private double yRotatedSurfaceArea(java.awt.geom.Area a) {
		//TODO: I think I need to handle seg_close!!
		PathIterator i = a.getPathIterator(new AffineTransform(), .001);
		double x = 0, y = 0;
		double mx = 0, my = 0;
		double len = 0;
		while (!i.isDone()) {
			double coords[] = new double[6];
			int type = i.currentSegment(coords);
			if (type == PathIterator.SEG_LINETO || type == PathIterator.SEG_CLOSE) {


				double nx = coords[0];
				double ny = coords[1];
				
				if ( type == PathIterator.SEG_CLOSE ){
					nx = mx;
					ny = my;
				}
				
				double dy = Math.abs(y-ny);
				double dx = Math.abs(x-nx);
				double xl = x>nx?x:nx;
				double xs = x<nx?x:nx;	
				
				double add = 0;
				if ( dx == 0 ){
					add = 2 * Math.PI * xl * dy;
				} else if ( dy == 0 ){
					 add = Math.PI * xl * xl - Math.PI * xs * xs;
				}else{
					double h = xl/dx * dy;
					double s1 = Math.sqrt(xl*xl + h*h);
					double s2 = Math.sqrt(xs*xs + (h-dy)*(h-dy));
					add = Math.PI * (xl*s1 - xs*s2);
				}
				
				len += add;

				x = nx;
				y = ny;
			} else if (type == PathIterator.SEG_MOVETO) {
				mx = x = coords[0];
				my = y = coords[1];
			} else {
				throw new Error("Non-flattened geometry!");
			}
			i.next();
		}
		return len;
	}
	

	
	private double area(java.awt.geom.Area a) {
		if ( !a.isSingular() )
			throw new IllegalArgumentException("Can not calculate area of non-singular shape!");
		PathIterator i = a.getPathIterator(new AffineTransform(), .001);
		
		
		double x = 0, y = 0, sx = 0, sy = 0;
		double nx, ny;
		double area = 0;
		while (!i.isDone()) {
			double coords[] = new double[6];
			int type = i.currentSegment(coords);
			switch( type ){
			case PathIterator.SEG_CLOSE:
				//Go back to the start
				nx = sx;
				ny = sy;
				area += x * ny;
				area -= y * nx;
				break;
			case PathIterator.SEG_LINETO:
				nx = coords[0];
				ny = coords[1];
				area += x * ny;
				area -= y * nx;

				//Remember the last points
				x = nx;
				y = ny;
				
				break;
			case PathIterator.SEG_MOVETO:
				//Remember the starting point
				x = sx = coords[0];
				y = sy = coords[1];
				break;
			default:
				throw new Error("Bad segment type from Flattening Path Iterator");
			}
			i.next();
		}
		
		area = area / 2.0; // Result so far is double the signed area
		
		if ( area < 0 ) //Depending on winding it could be negative
			area = area * -1.0;
		
		
		return area;
	}

	
	public class FancyPathIterator extends FlatteningPathIterator{

		public FancyPathIterator(PathIterator path, double error) {
			super(path, error);
		}
		
		
		
	}
	
	public static void main(String args[]){
		JFrame f = new JFrame();
		f.setContentPane(new QuadTest());
		f.setDefaultCloseOperation(f.DISPOSE_ON_CLOSE);
		f.setSize(1500,800);
		f.setLocation(1600, 100);
		f.show();
	}
}
