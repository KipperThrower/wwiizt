package pl.wwizt.vector.distances;

import java.util.Comparator;
import java.util.List;

import pl.wwiizt.vector.model.Hint;

public interface Distance {

	public double measureDistance(List<Double> thisVector, List<Double> thatVector);

	public Comparator<Hint> getComparator();
	
}
