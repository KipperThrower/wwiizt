package pl.wwiizt.vector.model;

import com.google.common.primitives.Doubles;

public class Hint implements Comparable<Hint> {

	private double rank;
	private String path;

	public double getRank() {
		return rank;
	}

	public void setRank(double rank) {
		this.rank = rank;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public int compareTo(Hint o) {
		if (rank == o.rank) {
			return 0;
		}
		return -Doubles.compare(rank, o.rank);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("===================================\n");
		sb.append("File: ");
		sb.append(path);
		sb.append("\n");
		sb.append("Rank: ");
		sb.append(rank);
		sb.append("\n");
		sb.append("===================================");
		return sb.toString();
	}

}
