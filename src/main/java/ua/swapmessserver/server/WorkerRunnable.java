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
import java.util.concurrent.ConcurrentLinkedDeque;
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

    private boolean isStopped = false;
    private int code = 0;
    protected Socket clientSocket = null;
    protected ThreadPooledServer server = null;
    protected InputStream input = null;
    protected OutputStream output = null;

    public WorkerRunnable(Socket clientSocket, ThreadPooledServer server) {
        try {
            this.clientSocket = clientSocket;
            this.server = server;
            this.input = clientSocket.getInputStream();
            this.output = clientSocket.getOutputStream();
        } catch (IOException ex) {
            Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        Message mess = null;
        // Читаем первое сообщение в котором пробуем авторизироватся
        if (ReadAuthMessage()) {
            SendAuthMessage();
            if (!App.listUsersConcurrentHashMapObject.containsValue(this)) {
                App.listUsersConcurrentHashMapObject.put(this.code, this);
            }
        }
        while (!this.isStopped()) {
            mess = this.ReadMessage();
            if (mess != null) {
                this.server.Handle(mess);
                //App.listMessagesConcurrentLinkedDeque.add(mess);
            }
        }
        
//            long time = System.currentTimeMillis();
//            output.write(("HTTP/1.1 200 OK\n\nWorkerRunnable: "
//                    + this.serverText + " - "
//                    + time
//                    + "").getBytes());
//
//            System.out.println("Request processed: " + time);
    }

    /**
     * Check user from outer repository
     * @param tm
     * @return boolean
     */
    private boolean CheckUser(TechnicMessage tm) {
        UserStub us = new UserStub();
        if (tm != null) {
            this.code = us.CheckUser(tm);
            return true;
        }
        return false;
    }

    /**
     * Read authentification message from input stream and unmarshal to object <code>TechnicMessage</code>
     * and check user.
     * @return boolean
     */
    private boolean ReadAuthMessage() {
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
                tm = (TechnicMessage) u.unmarshal(this.input);
            } catch (JAXBException ex) {
                tm = null;
                Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return (tm != null && CheckUser(tm));
    }

    /**
     * Read message from input stream and unmarshal to object <code>Message</code>
     * @return 
     */
    private Message ReadMessage() {
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
                mess = (Message) u.unmarshal(this.input);
            } catch (JAXBException ex) {
                mess = null;
                Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return mess;
    }

    private void SendAuthMessage() {
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
                m.marshal(ok, this.output);
            } catch (JAXBException ex) {
                Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void SendMessage(Message mess) {
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
                m.marshal(mess, this.output);
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

    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop() {
        this.isStopped = true;
//        try {
        this.close();
//        } catch (IOException e) {
//            throw new RuntimeException("Error closing server", e);
//        }
    }

    private void close() {
        if (this.output != null) {
            try {
                this.output.close();
            } catch (IOException ex) {
                Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (this.input != null) {
            try {
                this.input.close();
            } catch (IOException ex) {
                Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (this.clientSocket != null) {
            try {
                this.clientSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
