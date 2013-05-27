package pl.wwizt.vector.distances;

import java.util.List;

public class CosineDistance implements Distance {

	@Override
	public double measureDistance(List<Double> thisVector, List<Double> thatVector) {
		double result = 0;
		
		for (int i=0; i < thisVector.size(); i++) 
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


}
