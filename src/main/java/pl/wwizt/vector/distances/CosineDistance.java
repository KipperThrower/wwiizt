package pl.wwizt.vector.distances;

import java.util.Comparator;
import java.util.List;

import pl.wwiizt.vector.model.Hint;

import com.google.common.primitives.Doubles;

public class CosineDistance implements Distance {

	@Override
	public double measureDistance(List<Double> thisVector, List<Double> thatVector) {
		double result = 0;

		for (int i = 0; i < thisVector.size(); i++)
			result += thisVector.get(i) * thatVector.get(i);

		result /= vectorLength(thisVector) * vectorLength(thatVector);

		return result;
	}

	private double vectorLength(List<Double> vector) {
		double measure = 0;

		for (int i = 0; i < vector.size(); i++)
			measure += vector.get(i) * vector.get(i);

		return Math.sqrt(measure);
	}

	@Override
	public Comparator<Hint> getComparator() {
		return new Comparator<Hint>() {

			@Override
			public int compare(Hint o1, Hint o2) {
//				double r1 = o1.getRank();
//				double r2 = o2.getRank();
//				
				
				//musiałem, bo java się pluła, że "Comparison method violates its general contract"
//				if (Double.isNaN(r1))
//					return 1;
//				
//				else if (Double.isNaN(r2))
//					return -1;
				
				
				return -Doubles.compare(o1.getRank(), o2.getRank());
			}

		};
	}
	
}
