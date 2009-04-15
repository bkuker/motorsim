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
	
	double r = 0;
	@Override
	public void paint(Graphics gg){
		Graphics2D g = (Graphics2D)gg;
		
		g.clearRect(0, 0, 600, 600);
		GeneralPath p;
		
		p = new GeneralPath();
		p.moveTo(100,100);
		p.lineTo(200, 150);
		p.lineTo(150, 180);
		p.lineTo(100, 300);
		p.closePath();
		g.setColor(Color.red);
		g.draw(growArea(p, r));
		g.setColor(Color.black);		
		g.draw(p);
		
		
		p = new GeneralPath();
		p.moveTo(10,10);
		p.lineTo(50, 20);
		p.lineTo(60, 30);
		p.closePath();
		g.setColor(Color.red);
		g.draw(growArea(p, 10));
		g.setColor(Color.black);		
		g.draw(p);
		
	}
	
	private Area growArea( Shape a, double sz ){
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
					if ( sz > 0 )
						rect = new Area(new Rectangle2D.Double(0,-sz,len,sz));
					else
						rect = new Area(new Rectangle2D.Double(0,0,len,-sz));
					rect.transform(AffineTransform.getRotateInstance(angle));
					rect.transform(AffineTransform.getTranslateInstance(last[0], last[1]));
					ret.add(rect);
					
					if ( sz > 0 ){
						ret.add(new Area(new Ellipse2D.Double(coords[0]-sz, coords[1]-sz, sz*2, sz*2)));
					} else {
						ret.add(new Area(new Ellipse2D.Double(coords[0]+sz, coords[1]+sz, -sz*2, -sz*2)));
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
		
		if ( sz > 0 )
			ret.add(new Area(a));
		else
			ret.intersect(new Area(a));
		return ret;
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
