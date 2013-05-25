package pl.wwiizt.helpers;

import java.util.ArrayList;
import java.util.List;


/**
 * Na podstawie WiWizI-wyk1_IR str 1.2
 * @author sadam
 *
 */
public class MeasuresHelper {

	private List<String> supposedResults;
	private List<String> actualResults;
	private List<String> relevantDocuments;

	private double precision; //dokladnosc
	private double recall; //kompletnosc
	private double fmeasure;

	//strange ones. to check!
	private double recallRank;
	private double logarithmicPrecision;

	private int cutoff;
	private int rank;

	public int getRank() {
		return rank;
	}

	public MeasuresHelper(List<String> supposedResults, List<String> actualResults, int cutoff) {
		this.supposedResults = supposedResults;
		this.actualResults = actualResults;
		this.cutoff = cutoff;


		getRelevant();
		rank = countRank(0);
		countPrecision();
		countRecall();
		countFMeasure();
		countRecallRank();
		countLogarithmicPrecision();

	}

	private void getRelevant() {
		relevantDocuments = new ArrayList<String>();

		for (String res : actualResults)
			if (supposedResults.contains(res))
				relevantDocuments.add(res);

	}

	private void countPrecision() {
		precision = (double) relevantDocuments.size() / (double) (actualResults.size() > 0 ? actualResults.size() : 1);
	}

	private void countRecall() {
		recall = (double) relevantDocuments.size() / cutoff;
	}

	private void countFMeasure() {
		//zeby nie bylo dzielenia przez zero 
		if (precision + recall == 0)
			fmeasure = 0;
		else
			fmeasure = 2 * (precision * recall) / (precision + recall);
	}

	private void countRecallRank() {
		recallRank = (double) relevantDocuments.size();

		double tmp = 0;

		for (String relevant : relevantDocuments)
			tmp += actualResults.indexOf(relevant) + 1;

		recallRank /= tmp;
	}

	private void countLogarithmicPrecision() {
		double nominator = 0;
		double denominator = 0;

		for (int i = 0; i < relevantDocuments.size(); i++) {
			nominator += Math.log10(i + 1);
			denominator += Math.log10(countRank(i) + 1);
		}
		
		logarithmicPrecision = nominator / denominator;
	}
	
	private int countRank(int i) {
		if (relevantDocuments.isEmpty() || !actualResults.contains(relevantDocuments.get(i)))
			return 0;
		
		return actualResults.indexOf(relevantDocuments.get(i)) +1;
	}
	
	public double getPrecision() {
		return precision;
	}

	public double getRecall() {
		return recall;
	}

	public double getFmeasure() {
		return fmeasure;
	}

	public double getRecallRank() {
		return recallRank;
	}

	public double getLogarithmicPrecision() {
		return logarithmicPrecision;
	}


}
