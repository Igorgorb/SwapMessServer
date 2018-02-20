/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.messages;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author a119
 */
@XmlType(propOrder = {"reciverName","reciverCode", "messageText"})
@XmlRootElement(name = "Message")
public class Message {

    String reciverName;
    Integer reciverCode;
    String messageText;

    public String getReciverName() {
        return reciverName;
    }

    public void setReciverName(String reciverName) {
        this.reciverName = reciverName;
    }

    public Integer getReciverCode() {
        return reciverCode;
    }

    public void setReciverCode(Integer reciverCode) {
        this.reciverCode = reciverCode;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
