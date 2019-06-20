package model;

import java.util.List;
import java.util.Map;

import deltaiot.services.LinkSettings;

public class Adaptation {
	private Map<String, List<LinkSettings>> motesAdaptation;

	public Map<String, List<LinkSettings>> getMotesAdaptation() {
		return motesAdaptation;
	}

	public void setMotesAdaptation(Map<String, List<LinkSettings>> motesAdaptation) {
		this.motesAdaptation = motesAdaptation;
	}

}
