package asap.faceengine.lipsync;

/**
 * A simple class serving as an extended data type to store all parameters for a dominance function.
 * 
 * @author mklemens
 */
public class DominanceParameters{
	
	private double magnitude;
	private double stretchLeft;
	private double stretchRight;
	private double rateLeft;
	private double rateRight;
	private double peak;
	private double startOffsetMultiplicator;
	private double endOffsetMultiplicator;
	
	public DominanceParameters(Double m, Double sl, Double sr, Double rl, Double rr, Double p, Double tol, Double tor) {
		setDominanceParameters(m, sl, sr, rl, rr, p, tol, tor);
	}
	
	// Store the passed parameters
	public void setDominanceParameters(Double m, Double sl, Double sr, Double rl, Double rr, Double p, Double tol, Double tor) {
		magnitude = m;
		stretchLeft = sl;
		stretchRight = sr;
		rateLeft = rl;
		rateRight = rr;
		peak = p;
		startOffsetMultiplicator = tol;
		endOffsetMultiplicator = tor;
	}
	
	public double getMagnitude() {
		return magnitude;
	}

	public double getStretchLeft() {
		return stretchLeft;
	}

	public double getStretchRight() {
		return stretchRight;
	}

	public double getRateLeft() {
		return rateLeft;
	}

	public double getRateRight() {
		return rateRight;
	}

	public double getPeak() {
		return peak;
	}
	
	public double getStartOffsetMultiplicator() {
		return startOffsetMultiplicator;
	}

	public double getEndOffsetMultiplicator() {
		return endOffsetMultiplicator;
	}
}