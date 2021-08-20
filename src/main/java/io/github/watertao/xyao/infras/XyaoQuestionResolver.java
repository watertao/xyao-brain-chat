package io.github.watertao.xyao.infras;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class XyaoQuestionResolver implements MessageListener {

  private static final Logger logger = LoggerFactory.getLogger(XyaoQuestionResolver.class);

  private ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private Environment env;

  @Autowired
  private XyaoChannelProxy channelProxy;

  @Autowired
  private Chatter chatter;



  public void onMessage(Message message, byte[] pattern) {

    String messageStr = message.toString();

    logger.info("[ >>> ] " + messageStr);

    XyaoQuestion question = null;
    try {
      question = objectMapper.readValue(message.toString(), XyaoQuestion.class);
    } catch (JsonProcessingException e) {
      logger.error(e.getMessage(), e);
      return;
    }

    try {
      handleQuestion(question);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      makeErrResponse(question, e);
    }

  }

  private void handleQuestion(XyaoQuestion question) {

    String answer = chatter.answer(question.getText(), question.getSender().getId());

    XyaoAnswer message = new XyaoAnswer();
    message.setEnv(question.getEnv());
    message.setRoom(question.getRoom());
    message.getReceivers().add(new XyaoAnswer.Contact(){{
      setId(question.getSender().getId());
      setName(question.getSender().getName());
      setIsMention(false);
    }});
    message.getEntities().add(new XyaoAnswer.StringEntity(answer));

    channelProxy.publish(message);

  }

  private void makeErrResponse(XyaoQuestion question, Exception exception) {
    XyaoAnswer message = new XyaoAnswer();
    message.setEnv(question.getEnv());
    message.setRoom(question.getRoom());
    message.getReceivers().add(new XyaoAnswer.Contact(){{
      setId(question.getSender().getId());
      setName(question.getSender().getName());
      setIsMention(false);
    }});
    message.getEntities().add(new XyaoAnswer.StringEntity());

    ((XyaoAnswer.StringEntity) message.getEntities().get(0)).setPayload(
      exception.getMessage()
    );

    channelProxy.publish(message);

  }


}
