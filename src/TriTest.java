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
import java.awt.geom.Rectangle2D;

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
			Area ret = new Area();	
			
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
						//Do it;
						double len = Math.sqrt(Math.pow(last[0] - coords[0], 2) + Math.pow(last[1] - coords[1], 2));
						double dx = coords[0]-last[0];
						double dy = coords[1]-last[1];
						double angle = Math.atan2(dy, dx);
								
						Area rect;
						if ( regression > 0 )
							rect = new Area(new Rectangle2D.Double(0,0,len,regression));
						else
							rect = new Area(new Rectangle2D.Double(0,regression,len,-regression));
						rect.transform(AffineTransform.getRotateInstance(angle));
						rect.transform(AffineTransform.getTranslateInstance(last[0], last[1]));
						ret.add(rect);
						
						if ( regression > 0 ){
							ret.add(new Area(new Ellipse2D.Double(coords[0]-regression, coords[1]-regression, regression*2, regression*2)));
						} else {
							ret.add(new Area(new Ellipse2D.Double(coords[0]+regression, coords[1]+regression, -regression*2, -regression*2)));
						}
						
						
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
				ret.add(a);
			}else{
				Area acp = (Area)a.clone();
				acp.subtract(ret);
				ret = acp;
			}
			return ret;
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

		g.setColor(Color.red);
		g.draw(shape.getRegressedShape(r));
		//g.setColor(Color.black);		
		//g.draw(shape.getRegressedShape(0));
		
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
					@Override
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
