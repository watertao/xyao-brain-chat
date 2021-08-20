package io.github.watertao.xyao.infras;

import javax.security.auth.login.Configuration;

public class XyaoQuestion {

//  brain: string
//
//  /**
//   * the message sent to x.yao in Room or Whisper
//   */
//  env: 'ROOM' | 'WHISPER'
//
//  /**
//   * who send this instruction
//   */
//  sender: InstructionSender
//
//  /**
//   * if message is sent in Room, this field should be set
//   */
//  room: Room | null
//
//  /**
//   * the message after brain prefix, for example
//   * message is 'x:dice 100' , the text will be 'dice 100'
//   */
//  text: string

  private String brain;
    private String env;
    private Contact sender;
    private Room room;
    private String text;

  public String getEnv() {
    return env;
  }

  public void setEnv(String env) {
    this.env = env;
  }

  public String getBrain() {
    return brain;
  }

  public void setBrain(String brain) {
    this.brain = brain;
  }

  public Contact getSender() {
    return sender;
  }

  public void setSender(Contact sender) {
    this.sender = sender;
  }

  public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static class Room {
        private String id;
        private String topic;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }
    }

    public static class Contact {
        private String id;
        private String name;
        private boolean isMaster;

      public boolean getIsMaster() {
        return isMaster;
      }

      public void setIsMaster(boolean master) {
        isMaster = master;
      }

      public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
