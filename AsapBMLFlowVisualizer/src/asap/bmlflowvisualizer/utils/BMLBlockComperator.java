package asap.bmlflowvisualizer.utils;

import java.util.Comparator;

import asap.bmlflowvisualizer.gui.VisualisationField;

public class BMLBlockComperator implements Comparator<BMLBlock> {

	private VisualisationField type;

	public BMLBlockComperator(VisualisationField type) {
		this.type = type;
	}

	@Override
	public int compare(BMLBlock o1, BMLBlock o2) {
		switch (type) {
		case HistPlanned:
			return (Long.compare(o1.getPlanStart(), o2.getPlanStart()));
		case HistScheduled:
			return (Long.compare(o1.getSchedStart(), o2.getSchedStart()));
		case HistPlayed:
			return (Long.compare(o1.getPlayStart(), o2.getPlayStart()));
		default:
			return 0;
		}
	}

}
