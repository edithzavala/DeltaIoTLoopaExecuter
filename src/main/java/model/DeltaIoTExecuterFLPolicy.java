package model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeltaIoTExecuterFLPolicy {
	private List<String> motes;

	public List<String> getMotes() {
		return motes;
	}

	public void setMotes(List<String> motes) {
		this.motes = motes;
	}

}
