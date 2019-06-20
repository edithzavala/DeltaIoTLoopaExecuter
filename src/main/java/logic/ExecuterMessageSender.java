package logic;

import org.loopa.comm.message.IMessage;
import org.loopa.element.sender.messagesender.AMessageSender;

import client.DeltaIoTClient;

public class ExecuterMessageSender extends AMessageSender {
	private DeltaIoTClient me = new DeltaIoTClient();

	@Override
	public void processMessage(IMessage mssg) {
		if (mssg.getMessageBody().get("type").equals("ME_IoTNetwork")) {
			me.postAdaptation((String) this.getComponent().getComponentRecipient("IoTNetwork").getRecipient(), mssg);
		}
	}
}
