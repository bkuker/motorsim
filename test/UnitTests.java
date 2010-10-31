import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.billkuker.rocketry.motorsim.test.ConvergentDivergentNozzleTest;
import com.billkuker.rocketry.motorsim.test.CoredCylindricalGrainTest;
import com.billkuker.rocketry.motorsim.test.CylindricalChamberTest;
import com.billkuker.rocketry.motorsim.test.KNSUTest;
import com.billkuker.rocketry.motorsim.test.MotorIOTest;
import com.billkuker.rocketry.motorsim.test.RocketScienceTest;
import com.billkuker.rocketry.motorsim.test.ShapeUtilTest;

@RunWith(Suite.class)
@SuiteClasses({ ConvergentDivergentNozzleTest.class,
		CoredCylindricalGrainTest.class, CylindricalChamberTest.class,
		KNSUTest.class, MotorIOTest.class, ShapeUtilTest.class, RocketScienceTest.class })
public class UnitTests {

}
