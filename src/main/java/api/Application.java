package api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.LoopAElementMessageCode;
import org.loopa.comm.message.Message;
import org.loopa.comm.message.MessageType;
import org.loopa.comm.message.PolicyConfigMessageBody;
import org.loopa.element.functionallogic.enactor.IFunctionalLogicEnactor;
import org.loopa.element.functionallogic.enactor.executer.ExecuterFunctionalLogicEnactor;
import org.loopa.element.sender.messagesender.IMessageSender;
import org.loopa.executer.Executer;
import org.loopa.executer.IExecuter;
import org.loopa.policy.IPolicy;
import org.loopa.policy.Policy;
import org.loopa.recipient.Recipient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.databind.ObjectMapper;

import logic.ExecuterMessageSender;
import logic.FunctionalLogicEnactorManager;
import model.DeltaIoTExecuterSenderPolicy;

@SpringBootApplication
public class Application {
	public static String EXECUTER_ID;
	public static IExecuter ex;

	// create Monitor with new FLE(manager), new Policy and new Sender.
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		EXECUTER_ID = args[0];
		String policyFilePath = "/tmp/config/" + args[1];
		
		/** Init policy (MANDATORY) **/
		Map<String, String> initPolicy = new HashMap<>();
		initPolicy.put(LoopAElementMessageCode.MSSGINFL.toString(), "1");
		initPolicy.put(LoopAElementMessageCode.MSSGINAL.toString(), "2");
		initPolicy.put(LoopAElementMessageCode.MSSGADAPT.toString(), "3");
		initPolicy.put(LoopAElementMessageCode.MSSGOUTFL.toString(), "4");
		initPolicy.put(LoopAElementMessageCode.MSSGOUTAL.toString(), "5");
		
		/****** Create executer ***/
		// System.out.println(policyContent.toString());
		IMessageSender sMS = new ExecuterMessageSender();
		IFunctionalLogicEnactor flE = new ExecuterFunctionalLogicEnactor(new FunctionalLogicEnactorManager());
		IPolicy ep = new Policy(EXECUTER_ID, initPolicy);
		ex = new Executer(EXECUTER_ID, ep, flE, sMS);
		ex.construct();
		
		/***** Add logic policies ****/
		String policyString = "";
		Map<String, String> policyContent = new HashMap<>();
		try {
			policyString = new String(Files.readAllBytes(Paths.get(policyFilePath)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		policyContent.put("config", policyString);
		PolicyConfigMessageBody messageContentFL = new PolicyConfigMessageBody(ex.getFunctionalLogic().getComponentId(),
				policyContent);
		IMessage mssgAdaptFL = new Message(EXECUTER_ID, ex.getReceiver().getComponentId(), 2,
				MessageType.REQUEST.toString(), messageContentFL.getMessageBody());
		ex.getReceiver().doOperation(mssgAdaptFL);
		
		/*** Add recipients and corresponding policies ****/
		DeltaIoTExecuterSenderPolicy executerRecipients;
		ObjectMapper mapper = new ObjectMapper();
		try {
			executerRecipients = mapper.readValue(policyString, DeltaIoTExecuterSenderPolicy.class);
			executerRecipients.getRecipients().forEach(recepient -> {
				ex.addElementRecipient(
						new Recipient(recepient.getId(), recepient.getTypeOfData(), recepient.getRecipient()));
			});
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
