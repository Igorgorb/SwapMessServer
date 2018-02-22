/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.swapmessserver.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
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

    // JAXB данные для работы с сообщениями
    private JAXBContext jcTM = null;
    private Unmarshaller uTM = null;
    private Marshaller mTM = null;

    private JAXBContext jcOK = null;
    private Unmarshaller uOK = null;
    private Marshaller mOK = null;

    private JAXBContext jcM = null;
    private Unmarshaller uM = null;
    private Marshaller mM = null;

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
        try {
            InitJAXB();
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
        } catch (JAXBException ex) {
            Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Check user from outer repository
     *
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
     * Read authentification message from input stream and unmarshal to object
     * <code>TechnicMessage</code> and check user.
     *
     * @return boolean
     */
    private boolean ReadAuthMessage() {
        TechnicMessage tm = null;
        System.out.println("ReadAuthMessage");
        try {
            String s = readFromSocket(TechnicMessage.XMLELEMENTNAME);
            System.out.println(s);
            tm = (TechnicMessage) this.uTM.unmarshal(new StringReader(s));
        } catch (JAXBException ex) {
            tm = null;
            Logger.getLogger(WorkerRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }

        return (tm != null && CheckUser(tm));
    }

    /**
     * Read message from input stream and unmarshal to object
     * <code>Message</code>
     *
     * @return
     */
    private Message ReadMessage() {
        Message mess = null;
        String s = readFromSocket(Message.XMLELEMENTNAME);
        try {
            mess = (Message) this.uM.unmarshal(new StringReader(s));

        } catch (JAXBException ex) {
            Logger.getLogger(WorkerRunnable.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return mess;
    }

    private void SendAuthMessage() {
        System.out.println("SendAuthMessage");
        Ok ok = new Ok();
        ok.setMess("Ok");
        try {
            this.mOK.marshal(ok, this.output);
        } catch (JAXBException ex) {
            Logger.getLogger(WorkerRunnable.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void SendMessage(Message mess) {
        System.out.println("SendMessage");

        try {
            this.mM.marshal(mess, this.output);
        } catch (JAXBException ex) {
            Logger.getLogger(WorkerRunnable.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void Suspend(int millisec) {
        try {
            Thread.sleep(millisec);

        } catch (InterruptedException ex) {
            Logger.getLogger(WorkerRunnable.class
                    .getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(WorkerRunnable.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (this.input != null) {
            try {
                this.input.close();

            } catch (IOException ex) {
                Logger.getLogger(WorkerRunnable.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (this.clientSocket != null) {
            try {
                this.clientSocket.close();

            } catch (IOException ex) {
                Logger.getLogger(WorkerRunnable.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    /**
     * Init JAXB context for all messages
     *
     * @throws JAXBException
     */
    private void InitJAXB() throws JAXBException {
        System.out.println("InitJAXB");

        this.jcTM = JAXBContext.newInstance(TechnicMessage.class);
        this.uTM = this.jcTM.createUnmarshaller();
        this.mTM = this.jcTM.createMarshaller();

        this.jcOK = JAXBContext.newInstance(Ok.class);
        this.uOK = this.jcOK.createUnmarshaller();
        this.mOK = this.jcOK.createMarshaller();

        this.jcM = JAXBContext.newInstance(Message.class);
        this.uM = this.jcM.createUnmarshaller();
        this.mM = this.jcM.createMarshaller();
    }

    /**
     * Read from socket's stream and not blocking work, when not sent
     * EndOfStream
     *
     * @param expectedMessage - name expected message
     * @return String - readed message
     */
    private String readFromSocket(String expectedMessage) {
        String result = "";
        try {
            // prepare a stream to read the XML document
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(this.input);

            // prepare an unmarshaller to turn the XML into objects
            //JAXBContext context = JAXBContext.newInstance(Test.class);
            //Unmarshaller unmarshaller = context.createUnmarshaller();
            //JAXBElement<Test> unmarshalledObj = unmarshaller.unmarshal(reader, Test.class);
            //Test item = unmarshalledObj.getValue();
            // get first event for the XML parsing
            int readerEvent = reader.next();

            // keep going until we reach the end of the document
            while (readerEvent != XMLStreamConstants.END_DOCUMENT) {

                // keep unmarshalling for every element of the expected type
                while (readerEvent == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(expectedMessage)) {
//                    result = reader.getText();
                    // The unmarshaller will have moved the pointer in the 
                    //  stream reader - we should now be pointing at the 
                    //  next event immediately after the unmarshalled 
                    //  element. 
                    // This will either the start of the next element, or 
                    //  CHARACTERS if there is whitespace in between them, 
                    //  or something else like a comment. 
                    // We need to check this to decide whether we can 
                    //  unmarshall again, or if we need to move the 
                    //  stream reader on to get to the next element. 
                    readerEvent = reader.getEventType();
                }

                // move the stream reader on to the next element
                readerEvent = reader.next();
            }
            result = reader.getText();
            // reached the end of the document - close the reader
            reader.close();

        } catch (Exception ex) {
            Logger.getLogger(WorkerRunnable.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
