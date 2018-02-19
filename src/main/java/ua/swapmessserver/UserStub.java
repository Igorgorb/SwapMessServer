/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.swapmessserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ua.messages.TechnicMessage;

/**
 *
 * @author a119
 */
public class UserStub {

    private static Map<String, String> listUsersStubConcurrentHashMapObject = null;

    public UserStub() {
        listUsersStubConcurrentHashMapObject = new ConcurrentHashMap<>(10);
        listUsersStubConcurrentHashMapObject.put("igor", "123");
        listUsersStubConcurrentHashMapObject.put("gorb", "321");
        listUsersStubConcurrentHashMapObject.put("ig", "147");
        listUsersStubConcurrentHashMapObject.put("qwe123", "145");
        listUsersStubConcurrentHashMapObject.put("ar", "412");
        listUsersStubConcurrentHashMapObject.put("kor", "987");
        listUsersStubConcurrentHashMapObject.put("ir", "231");
        listUsersStubConcurrentHashMapObject.put("kurt", "654");
        listUsersStubConcurrentHashMapObject.put("evgen", "951");
    }

    public synchronized Integer CheckUser(TechnicMessage tm) {
        if (listUsersStubConcurrentHashMapObject.containsKey(tm.getLogin())) {
            if (listUsersStubConcurrentHashMapObject.get(tm.getLogin()).equals(tm.getPass())) {
                return Integer.valueOf(tm.getPass());
            }
        }
        return Integer.valueOf("0");
    }

}
