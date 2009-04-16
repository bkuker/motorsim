import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class TriTest extends JPanel {
	
	private class RegressableShape {
		private Area a;
		public RegressableShape( Shape s ){
			if ( s instanceof Area )
				a = (Area)s;
			a = new Area(s);
		}
		
		public Area getRegressedShape(double regression){
			if ( regression == 0 )
				return a;
			
			//Build these separatly because intersecting the line
			//with the circle creates a small amount of error which
			//shows up when the resulting edge is no longer exactly
			//colinear with the original edge.
			Area rRect = new Area();	
			Area rCirc = new Area();
			
			PathIterator i = a.getPathIterator(new AffineTransform(), .001);
			double last[] = {0,0};
			double first[] = {0,0};

			while (!i.isDone()) {
				double coords[] = new double[6];
				int type = i.currentSegment(coords);
				switch (type){
					case PathIterator.SEG_MOVETO:
						first[0] = last[0] = coords[0];
						first[1] = last[1] = coords[1];
						break;
					case PathIterator.SEG_CLOSE:
						coords[0] = first[0];
						coords[1] = first[1];
					case PathIterator.SEG_LINETO:
						//Calculate the normal to this edge
						double dx = coords[0]-last[0];
						double dy = coords[1]-last[1];
						double len = Math.sqrt(dx*dx + dy*dy);						
						double normal[] = {-dy/len,dx/len};
						
						//Calculate the displacement of the endpoints
						//to create a rect
						double displacement[] = {regression*normal[0], regression*normal[1]};

						//Create that rect. Winding does not seem to matter...
						GeneralPath p = new GeneralPath();
						p.moveTo(last[0], last[1]);
						p.lineTo(last[0]+displacement[0], last[1]+displacement[1]);
						p.lineTo(coords[0]+displacement[0], coords[1]+displacement[1]);
						p.lineTo(coords[0], coords[1]);
						p.closePath();
						rRect.add( new Area(p));

						double er = Math.abs(regression);
						rCirc.add(new Area(new Ellipse2D.Double(coords[0]-er, coords[1]-er, 2*er, 2*er)));
						
						last[0] = coords[0];
						last[1] = coords[1];
						break;
					case PathIterator.SEG_CUBICTO:
					case PathIterator.SEG_QUADTO:
						throw new Error("Unflattend Geometry!");
				}
				i.next();
			}
			

			if ( regression > 0 ){
				//Combine all together
				rRect.add(a);
				rRect.add(rCirc);
			}else{
				Area acp = (Area)a.clone();
				acp.subtract(rRect);
				acp.subtract(rCirc);
				rRect = acp;
			}
			return rRect;
		}
	}
	
	double r = 0;
	
	RegressableShape shape;
	{
		GeneralPath p = new GeneralPath();
		p.moveTo(100,100);
		p.lineTo(200, 200);
		p.lineTo(100, 300);
		p.closePath();
		shape = new RegressableShape(p);
	}
	
	@Override
	public void paint(Graphics gg){
		Graphics2D g = (Graphics2D)gg;
		
		g.clearRect(0, 0, 600, 600);


		g.setColor(Color.black);
		g.fill(shape.getRegressedShape(r));
		g.setColor(Color.yellow);		
		g.draw(shape.getRegressedShape(0));
		g.setColor(Color.red);
		g.draw(shape.getRegressedShape(r));

		
	}
	
	
	public static void main( String args[] ){
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(f.DISPOSE_ON_CLOSE);
		final TriTest t = new TriTest();
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(t, BorderLayout.CENTER);
		f.getContentPane().add(new JSlider(){{
				setMinimum(-100);
				setMaximum(100);
				addChangeListener(new ChangeListener(){
					{ setValue(0); }
					public void stateChanged(ChangeEvent arg0) {
						t.r = getValue();
						t.repaint();
					}
				});
			}}, BorderLayout.SOUTH);

		f.setSize(400,400);
		f.setLocation(600,600);
		f.show();
		
	}
}
