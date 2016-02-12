/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.servlet;

/**
 *
 * @author Edward
 */
public class Emoticon {
    
    String code;
    String path;

    public Emoticon(String code, String path) {
        this.code = code;
        this.path = path;
    }

    public String getCode() {
        return code;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return code + ": " + path;
    }
    
    
}
