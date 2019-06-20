package logic;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.LoopAElementMessageBody;
import org.loopa.comm.message.LoopAElementMessageCode;
import org.loopa.comm.message.Message;
import org.loopa.comm.message.MessageType;
import org.loopa.element.functionallogic.enactor.executer.IExecuterFleManager;
import org.loopa.generic.element.component.ILoopAElementComponent;
import org.loopa.policy.IPolicy;
import org.loopa.policy.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import deltaiot.services.LinkSettings;
import model.Adaptation;
import model.DeltaIoTExecuterFLPolicy;
import model.ExecuterLink;
import model.ExecuterMote;
import model.PlanDocument;
import model.PlanningStep;

public class FunctionalLogicEnactorManager implements IExecuterFleManager {
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
	private IPolicy allPolicy = new Policy(this.getClass().getName(), new HashMap<String, String>());
	private ILoopAElementComponent owner;
	private DeltaIoTExecuterFLPolicy policy;

	@Override
	public void processLogicData(Map<String, String> data) {
		if (data.keySet().contains("execute")) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				PlanDocument motesMessage = mapper.readValue(data.get("execute"), PlanDocument.class);
				boolean addMote;
				List<ExecuterMote> motesEffected = new LinkedList<ExecuterMote>();
				for (ExecuterMote mote : motesMessage.getMotes()) {
					addMote = false;
					for (PlanningStep step : motesMessage.getSteps()) {
						if (step.getLink().getSource() == mote.getMoteid()) {
							addMote = true;
							if (step.getStep() == Step.CHANGE_POWER) {
								LOGGER.info("Change power from:" + mote.getLinkWithDest(step.getLink().getDest()).getPower() + " to:" + step.getValue());
								mote.getLinkWithDest(step.getLink().getDest()).setPower(step.getValue());
							} else if (step.getStep() == Step.CHANGE_DIST) {
								mote.getLinkWithDest(step.getLink().getDest()).setDistribution(step.getValue());
								LOGGER.info("Change distribution from:" + String.valueOf(mote.getMoteid()));
							}
						}
					}
					if (addMote) {
						motesEffected.add(mote);
					}
				}
				Map<String, List<LinkSettings>> motesAdaptation = new HashMap<>();
				for (ExecuterMote mote : motesEffected) {
					List<LinkSettings> newSettings = new LinkedList<LinkSettings>();
					for (ExecuterLink link : mote.getLinks()) {
						newSettings.add(new LinkSettings(mote.getMoteid(), link.getDest(), link.getPower(),
								link.getDistribution(), link.getSF()));
					}
					motesAdaptation.put(String.valueOf(mote.getMoteid()), newSettings);
				}
				Adaptation adaptation = new Adaptation();
				adaptation.setMotesAdaptation(motesAdaptation);
				sendAdaptationToEffect(mapper.writeValueAsString(adaptation));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendAdaptationToEffect(String adaptation) {
		LoopAElementMessageBody messageContent = new LoopAElementMessageBody("ME", adaptation);
		String code = getComponent().getElement().getElementPolicy().getPolicyContent()
				.get(LoopAElementMessageCode.MSSGOUTFL.toString());
		IMessage mssg = new Message(owner.getComponentId(), allPolicy.getPolicyContent().get(code),
				Integer.parseInt(code), MessageType.REQUEST.toString(), messageContent.getMessageBody());
		((ILoopAElementComponent) owner.getComponentRecipient(mssg.getMessageTo()).getRecipient()).doOperation(mssg);
	}

	@Override
	public void setConfiguration(Map<String, String> config) {
		LOGGER.info("Component (re)configured");
		if (config.containsKey("config")) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				this.policy = mapper.readValue(config.get("config"), DeltaIoTExecuterFLPolicy.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.allPolicy.update(new Policy(this.allPolicy.getPolicyOwner(), config));
	}

	@Override
	public ILoopAElementComponent getComponent() {
		return this.owner;
	}

	@Override
	public void setComponent(ILoopAElementComponent c) {
		this.owner = c;

	}

}
