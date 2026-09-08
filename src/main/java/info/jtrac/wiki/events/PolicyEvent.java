package info.jtrac.wiki.events;

import info.jtrac.wiki.Model;
import info.jtrac.wiki.Policy;

public class PolicyEvent {

	private Model model;
	private Policy policy;

	public PolicyEvent (Policy policy, Model model) {
		this.policy = policy;
		this.model = model;
	}

	public Policy getPolicy() {
		return policy;
	}

	public Model getModel() {
		return model;
	}
}

