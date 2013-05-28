package pl.wwizt.vector.distances;

import java.util.Comparator;
import java.util.List;

import com.google.common.primitives.Doubles;

import pl.wwiizt.vector.model.Hint;

public class EuclidesDistance implements Distance {

	@Override
	public double measureDistance(List<Double> thisVector, List<Double> thatVector) {
		double measure = 0;
		
		for (int i = 0; i < thisVector.size(); i++)
			measure += (thisVector.get(i) -  thatVector.get(i)) * (thisVector.get(i) -  thatVector.get(i));
		
		return Math.sqrt(measure);
	}

	@Override
	public Comparator<Hint> getComparator() {
		return new Comparator<Hint>() {

			@Override
			public int compare(Hint o1, Hint o2) {
				return Doubles.compare(o1.getRank(), o2.getRank());
			}
			
		};
	}

}
