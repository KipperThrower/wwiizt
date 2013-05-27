package pl.wwizt.vector.distances;

import java.util.List;

public class EuclidesDistance implements Distance {

	@Override
	public double measureDistance(List<Double> thisVector, List<Double> thatVector) {
		double measure = 0;
		
		for (int i = 0; i < thisVector.size(); i++)
			measure += (thisVector.get(i) -  thatVector.get(i)) * (thisVector.get(i) -  thatVector.get(i));
		
		return Math.sqrt(measure);
	}

}
