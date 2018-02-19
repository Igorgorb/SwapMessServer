/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.swapmessserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ua.swapmessserver.server.ThreadPooledServer;

/**
 *
 * @author a119
 */
public class App {

    public static int INIT_USER_COUNT = 50;
    public static Map<Integer, Runnable> listUsersConcurrentHashMapObject = null;

    
//    public void put(String s, int i) {
//        listUsersConcurrentHashMapObject.put(s, i);
//    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        listUsersConcurrentHashMapObject = new ConcurrentHashMap<>(INIT_USER_COUNT);
        App a = new App();

        ThreadPooledServer server = new ThreadPooledServer(12300);
        new Thread(server).start();
        try {
            Thread.sleep(200 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Stopping server");
        server.stop();
    }

}
