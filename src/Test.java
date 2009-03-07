import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;


public class Test {
	public static void main(String args[]){
		Amount<Length> l = (Amount<Length>)Amount.valueOf(1, SI.METER);
		System.out.println(l.divide(l).to(Dimensionless.UNIT));
		Amount<Length> m = (Amount<Length>)Amount.valueOf(1, 10000000000000000.0, SI.METER);
		System.out.println(m);
	
	}
}
