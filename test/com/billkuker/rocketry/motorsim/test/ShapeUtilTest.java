package com.billkuker.rocketry.motorsim.test;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.jscience.physics.amount.Amount;
import org.junit.Test;

import com.billkuker.rocketry.motorsim.grain.util.ShapeUtil;


public class ShapeUtilTest extends AbstractRocketTest{

	Area tenByTen = new Area(new Rectangle2D.Double(-5, -5, 10, 10));
	Area oneByOne = new Area(new Rectangle2D.Double(-.5, -.5, 1, 1));
	Area twoByTwoFar = new Area( new Rectangle2D.Double(30,30,2,2));
	
	Area r2Circle = new Area(new Ellipse2D.Double(-2,-2,4,4));


	Amount<javax.measure.quantity.Area> diff = Amount.valueOf(.01, sqMM);
	
	@Test
	public void testArea(){
		assertApproximate(ShapeUtil.area(tenByTen), Amount.valueOf(100, sqMM));
		assertApproximate(ShapeUtil.area(oneByOne), Amount.valueOf(1, sqMM));
		assertApproximate(ShapeUtil.area(twoByTwoFar), Amount.valueOf(4, sqMM));
		assertApproximate(ShapeUtil.area(r2Circle), Amount.valueOf(4*Math.PI, sqMM), diff);
		
		Area a = new Area(tenByTen);
		a.subtract(oneByOne);
		
		assertApproximate(ShapeUtil.area(a), ShapeUtil.area(tenByTen).minus(ShapeUtil.area(oneByOne)));
		
		a.add(twoByTwoFar);
		
		assertApproximate(ShapeUtil.area(a), 
				ShapeUtil.area(tenByTen)
					.minus(ShapeUtil.area(oneByOne))
					.plus(ShapeUtil.area(twoByTwoFar)));
		
		a = new Area(tenByTen);
		a.subtract(r2Circle);
		
		assertApproximate(ShapeUtil.area(a), ShapeUtil.area(tenByTen).minus(ShapeUtil.area(r2Circle)));
	}
}
