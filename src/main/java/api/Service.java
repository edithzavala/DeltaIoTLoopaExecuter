package api;

import java.util.HashMap;
import java.util.Map;

import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.LoopAElementMessageCode;
import org.loopa.comm.message.Message;
import org.loopa.comm.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Service {
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());

	@PostMapping("/execute")
	public ResponseEntity<String> createMonDataEntry(@RequestBody String planDocument) {
		if (planDocument != null) {
			Map<String, String> messageBody = new HashMap<>();
			messageBody.put("execute", planDocument);
			IMessage planMssg = new Message("EXECUTE", Application.ex.getElementId(),
					Integer.parseInt(Application.ex.getElementPolicy().getPolicyContent()
							.get(LoopAElementMessageCode.MSSGINFL.toString())),
					MessageType.REQUEST.toString(), messageBody);
			Application.ex.getReceiver().doOperation(planMssg);
			return ResponseEntity.status(HttpStatus.CREATED).build();
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	}
}
