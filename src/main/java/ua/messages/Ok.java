/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.messages;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author a119
 */
//@XmlType(propOrder = {"mess"})
@XmlRootElement//(name = "OK")
public class Ok {
    private String mess;

    public String getMess() {
        return mess;
    }

    @XmlElement
    public void setMess(String mess) {
        this.mess = mess;
    }
}
