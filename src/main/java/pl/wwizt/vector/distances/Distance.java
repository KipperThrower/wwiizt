package pl.wwizt.vector.distances;

import java.util.List;

public interface Distance {

	public double measureDistance(List<Double> thisVector, List<Double> thatVector);
	
}
