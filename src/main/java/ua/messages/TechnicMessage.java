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
//@XmlType(propOrder = {"login", "pass"})
@XmlRootElement(name = TechnicMessage.XMLELEMENTNAME)
public class TechnicMessage {

    public static final String XMLELEMENTNAME = "technicmessage";
    private String login;
    private String pass;

    public String getLogin() {
        return login;
    }

    @XmlElement
    public void setLogin(String login) {
        this.login = login;
    }

    public String getPass() {
        return pass;
    }

    @XmlElement
    public void setPass(String pass) {
        this.pass = pass;
    }

}
