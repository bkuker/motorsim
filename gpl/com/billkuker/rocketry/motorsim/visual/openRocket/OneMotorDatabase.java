package com.billkuker.rocketry.motorsim.visual.openRocket;
import java.util.List;
import java.util.Vector;

import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

import net.sf.openrocket.database.MotorDatabase;
import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorInstance;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Inertia;
import net.sf.openrocket.util.MathUtil;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.Burn.Interval;
import com.billkuker.rocketry.motorsim.BurnSummary;
import com.billkuker.rocketry.motorsim.ICylindricalChamber;
import com.billkuker.rocketry.motorsim.RocketScience;

public class OneMotorDatabase implements MotorDatabase {

	static Burn burn;
	static BurnSummary bs;
	static Coordinate cg[];
	static double[] time;
	static double[] thrust;

	static Motor motor = new Motor() {

		@Override
		public Type getMotorType() {
			return Type.SINGLE;
		}

		@Override
		public String getDesignation() {
			return bs.getRating();
		}

		@Override
		public String getDesignation(double delay) {
			return bs.getRating() + "-" + ((int) delay);
		}

		@Override
		public String getDescription() {
			if ( burn == null )
				return "NO MOTOR YET";
			return burn.getMotor().getName();
		}

		@Override
		public double getDiameter() {
			return ((ICylindricalChamber) burn.getMotor().getChamber()).getOD()
					.doubleValue(SI.METER);
		}

		@Override
		public double getLength() {
			return ((ICylindricalChamber) burn.getMotor().getChamber())
					.getLength().doubleValue(SI.METER);
		}

		@Override
		public String getDigest() {
			return "WTF";
		}

		@Override
		public MotorInstance getInstance() {

			return new MotorInstance() {

				private int position;

				// Previous time step value
				private double prevTime;

				// Average thrust during previous step
				private double stepThrust;
				// Instantaneous thrust at current time point
				private double instThrust;

				// Average CG during previous step
				private Coordinate stepCG;
				// Instantaneous CG at current time point
				private Coordinate instCG;

				private final double unitRotationalInertia;
				private final double unitLongitudinalInertia;

				private int modID = 0;

				{
					position = 0;
					prevTime = 0;
					instThrust = 0;
					stepThrust = 0;
					instCG = cg[0];
					stepCG = cg[0];
					unitRotationalInertia = Inertia
							.filledCylinderRotational(getDiameter() / 2);
					unitLongitudinalInertia = Inertia
							.filledCylinderLongitudinal(getDiameter() / 2,
									getLength());
				}

				@Override
				public double getTime() {
					return prevTime;
				}

				@Override
				public Coordinate getCG() {
					return stepCG;
				}

				@Override
				public double getLongitudinalInertia() {
					return unitLongitudinalInertia * stepCG.weight;
				}

				@Override
				public double getRotationalInertia() {
					return unitRotationalInertia * stepCG.weight;
				}

				@Override
				public double getThrust() {
					return stepThrust;
				}

				@Override
				public boolean isActive() {
					return prevTime < time[time.length - 1];
				}

				@Override
				public void step(double nextTime, double acceleration,
						AtmosphericConditions cond) {

					if (!(nextTime >= prevTime)) {
						// Also catches NaN
						throw new IllegalArgumentException(
								"Stepping backwards in time, current="
										+ prevTime + " new=" + nextTime);
					}
					if (MathUtil.equals(prevTime, nextTime)) {
						return;
					}

					modID++;

					if (position >= time.length - 1) {
						// Thrust has ended
						prevTime = nextTime;
						stepThrust = 0;
						instThrust = 0;
						stepCG = cg[cg.length - 1];
						return;
					}

					// Compute average & instantaneous thrust
					if (nextTime < time[position + 1]) {

						// Time step between time points
						double nextF = MathUtil.map(nextTime, time[position],
								time[position + 1], thrust[position],
								thrust[position + 1]);
						stepThrust = (instThrust + nextF) / 2;
						instThrust = nextF;

					} else {

						// Portion of previous step
						stepThrust = (instThrust + thrust[position + 1]) / 2
								* (time[position + 1] - prevTime);

						// Whole steps
						position++;
						while ((position < time.length - 1)
								&& (nextTime >= time[position + 1])) {
							stepThrust += (thrust[position] + thrust[position + 1])
									/ 2 * (time[position + 1] - time[position]);
							position++;
						}

						// End step
						if (position < time.length - 1) {
							instThrust = MathUtil.map(nextTime, time[position],
									time[position + 1], thrust[position],
									thrust[position + 1]);
							stepThrust += (thrust[position] + instThrust) / 2
									* (nextTime - time[position]);
						} else {
							// Thrust ended during this step
							instThrust = 0;
						}

						stepThrust /= (nextTime - prevTime);

					}

					// Compute average and instantaneous CG (simple average
					// between points)
					Coordinate nextCG;
					if (position < time.length - 1) {
						nextCG = MathUtil.map(nextTime, time[position],
								time[position + 1], cg[position],
								cg[position + 1]);
					} else {
						nextCG = cg[cg.length - 1];
					}
					stepCG = instCG.add(nextCG).multiply(0.5);
					instCG = nextCG;

					// Update time
					prevTime = nextTime;
				}

				@Override
				public MotorInstance clone() {
					try {
						return (MotorInstance) super.clone();
					} catch (CloneNotSupportedException e) {
						throw new BugException("CloneNotSupportedException", e);
					}
				}

				@Override
				public int getModID() {
					return modID;
				}
			};

		}

		@Override
		public Coordinate getLaunchCG() {
			return new Coordinate();
		}

		@Override
		public Coordinate getEmptyCG() {
			return new Coordinate();
		}

		@Override
		public double getBurnTimeEstimate() {
			return bs.thrustTime().doubleValue(SI.SECOND);
		}

		@Override
		public double getAverageThrustEstimate() {
			return bs.averageThrust().doubleValue(SI.NEWTON);
		}

		@Override
		public double getMaxThrustEstimate() {
			return bs.maxThrust().doubleValue(SI.NEWTON);
		}

		@Override
		public double getTotalImpulseEstimate() {
			return bs.totalImpulse().doubleValue(RocketScience.NEWTON_SECOND);
		}

		@Override
		public double[] getTimePoints() {
			return time;
		}

		@Override
		public double[] getThrustPoints() {
			return thrust;
		}

		@Override
		public Coordinate[] getCGPoints() {
			return cg;
		}

	};

	static void setBurn(Burn b) {
		burn = b;
		bs = new BurnSummary(b);

		cg = new Coordinate[burn.getData().size()];
		time = new double[burn.getData().size()];
		thrust = new double[burn.getData().size()];
		

		for (int i = 0; i < cg.length; i++) {
			cg[i] = new Coordinate();
			cg[i].setWeight(0.0); //TODO: Set weight!
		}

		int i = 0;
		for (Amount<Duration> t : burn.getData().keySet()) {
			time[i++] = t.doubleValue(SI.SECOND);
		}

		i = 0;
		for (Interval d : burn.getData().values()) {
			double t = d.thrust.doubleValue(SI.NEWTON);
			t = Math.max(t, 0.0001);
			thrust[i++] = t;
		}
		thrust[0] = 0;
		thrust[thrust.length - 1] = 0;
	}

	public OneMotorDatabase() {
		super();
	}

	public List<? extends Motor> findMotors(Motor.Type type,
			String manufacturer, String designation, double diameter,
			double length) {
		List<Motor> ret = new Vector<Motor>();
		System.err.println("Returning " + motor.getDescription());
		ret.add(motor);
		return ret;
	}

}
