/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.swapmessserver.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import ua.messages.Message;
import ua.messages.Ok;
import ua.messages.TechnicMessage;
import ua.swapmessserver.App;
import ua.swapmessserver.UserStub;

/**
 *
 * @author a119
 */
public class WorkerRunnable implements Runnable {

    private int code = 0;
    protected Socket clientSocket = null;
    protected String serverText = null;

    public WorkerRunnable(Socket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText = serverText;
    }

    @Override
    public void run() {
        try {
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            // Читаем первое сообщение в котором пробуем авторизироватся
            if (ReadAuthMessage(input)) {
                WriteAuthMessage(output);
                if (!App.listUsersConcurrentHashMapObject.containsValue(this)) {
                    App.listUsersConcurrentHashMapObject.put(this.code, this);
                }
            }

            
            long time = System.currentTimeMillis();
            output.write(("HTTP/1.1 200 OK\n\nWorkerRunnable: "
                    + this.serverText + " - "
                    + time
                    + "").getBytes());
            output.close();
            input.close();
            System.out.println("Request processed: " + time);
        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
    }

    private boolean CheckUser(TechnicMessage tm) {
        UserStub us = new UserStub();
        if (tm != null) {
            this.code = us.CheckUser(tm);
            return true;
        }
        return false;
    }

    private boolean ReadAuthMessage(InputStream input) {
        JAXBContext jc = null;
        Unmarshaller u = null;
        TechnicMessage tm = null;
        try {
            jc = JAXBContext.newInstance(TechnicMessage.class);
        } catch (JAXBException ex) {
            jc = null;
            Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (jc != null) {
            try {
                u = jc.createUnmarshaller();
            } catch (JAXBException ex) {
                u = null;
                Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (u != null) {
            try {
                tm = (TechnicMessage) u.unmarshal(input);
            } catch (JAXBException ex) {
                tm = null;
                Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return (tm != null && CheckUser(tm));
    }
    
    private Message ReadMessage(InputStream input) {
        JAXBContext jc = null;
        Unmarshaller u = null;
        Message mess = null;
        try {
            jc = JAXBContext.newInstance(TechnicMessage.class);
        } catch (JAXBException ex) {
            jc = null;
            Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (jc != null) {
            try {
                u = jc.createUnmarshaller();
            } catch (JAXBException ex) {
                u = null;
                Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (u != null) {
            try {
                mess = (Message) u.unmarshal(input);
            } catch (JAXBException ex) {
                mess = null;
                Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return mess;
    }

    private void WriteAuthMessage(OutputStream out) {
        JAXBContext jc = null;
        Marshaller m = null;
        Ok ok = new Ok();
        ok.setMess("Ok");
        try {
            jc = JAXBContext.newInstance(Ok.class);
        } catch (JAXBException ex) {
            jc = null;
            Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (jc != null) {
            try {
                m = jc.createMarshaller();
            } catch (JAXBException ex) {
                m = null;
                Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (m != null) {
            try {
                m.marshal(ok, out);
            } catch (JAXBException ex) {
                Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void WriteMessage(OutputStream out, Message mess) {
        JAXBContext jc = null;
        Marshaller m = null;
        try {
            jc = JAXBContext.newInstance(Message.class);
        } catch (JAXBException ex) {
            jc = null;
            Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (jc != null) {
            try {
                m = jc.createMarshaller();
            } catch (JAXBException ex) {
                m = null;
                Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (m != null) {
            try {
                m.marshal(mess, out);
            } catch (JAXBException ex) {
                Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void Suspend(int millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException ex) {
            Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
