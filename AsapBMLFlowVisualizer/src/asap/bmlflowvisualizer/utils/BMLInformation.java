package asap.bmlflowvisualizer.utils;

import java.io.Serializable;

public class BMLInformation implements Serializable {
	
	private long timestamp;
	private String information;
	private BMLInformationType type;
	
	public BMLInformation(long stamp, String info, BMLInformationType type) {
		timestamp = stamp;
		information = info;
		this.type = type;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getInformation() {
		return information;
	}

	public void setInformation(String information) {
		this.information = information;
	}

	
	public BMLInformationType getType() {
		return type;
	}

	public void setType(BMLInformationType type) {
		this.type = type;
	}

	public String toString(){
		return timestamp + information;		
	}
	
	
}
