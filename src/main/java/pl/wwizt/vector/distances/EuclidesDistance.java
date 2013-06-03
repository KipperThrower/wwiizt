package pl.wwizt.vector.distances;

import java.util.Comparator;
import java.util.List;

import com.google.common.primitives.Doubles;

import pl.wwiizt.vector.model.Hint;

public class EuclidesDistance implements Distance {

	@Override
	public double measureDistance(List<Double> thisVector, List<Double> thatVector) {
		double measure = 0;
		
		normalize(thisVector);
		normalize(thatVector);
		
		for (int i = 0; i < thisVector.size(); i++)
			measure += (thisVector.get(i) -  thatVector.get(i)) * (thisVector.get(i) -  thatVector.get(i));
		
		return Math.sqrt(measure);
	}
	
	private List<Double> normalize(List<Double> thisVector) {
		double measure = 0;
		
		for (int i = 0; i < thisVector.size(); i++)
			measure += thisVector.get(i) * thisVector.get(i);
		
		measure = Math.sqrt(measure);
		for (int i = 0; i < thisVector.size(); i++)
			thisVector.set(i, thisVector.get(i) / measure);
		
		return thisVector;
	}

	@Override
	public Comparator<Hint> getComparator() {
		return new Comparator<Hint>() {

			@Override
			public int compare(Hint o1, Hint o2) {
				double r1 = o1.getRank();
				double r2 = o2.getRank();
				
//				if (Double.isNaN(r1))
//					return 1;
//				
//				else if (Double.isNaN(r2))
//					return -1;
//				
				return Doubles.compare(r1, r2);
			}
			
		};
	}

}
