package SimulationPackage;
import java.util.Random;

public class Methods {
	public static double getNormal(double border95, double mean, Random ran) {
		return (ran.nextGaussian()*(border95/1.96) + mean);
	}
}
