/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.saga.java.mbox.ajug;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageBuilder;
import org.apache.james.mime4j.mboxiterator.CharBufferWrapper;
import org.apache.james.mime4j.mboxiterator.MboxIterator;
import org.apache.james.mime4j.message.DefaultMessageBuilder;

/**
 *
 * @author summers
 */
class MboxLoader {

    private static final CharsetEncoder ENCODER = Charset.forName("UTF-8").newEncoder();

    private static final String TO = "To:";
    private static final String FROM = "From:";
    private static final String DATE = "Date:";
    private static final String SUBJECT = "Subject:";
    private static final String CONTENT_TYPE = "Content-Type:";
    private static final String BOUNDARY = "boundary=";
    private static final String BOUNDARY_START  = "--";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String TEXT_HTML = "text/html";
    
    static void loadMbox(File mbox) {
        try {
            for (CharBufferWrapper message : MboxIterator.fromFile(mbox).charset(ENCODER.charset()).build()) {
               String str = message.toString();
               String to = safeGet(str, TO);
               String from = safeGet(str, FROM);
               String date  = safeGet(str, DATE);
               String subject = safeGet(str, SUBJECT);
               String contentType = safeGet(str, CONTENT_TYPE);
               String boundary = safeGet(contentType, BOUNDARY);

               String body = getPreferredBody(str, boundary);
                System.out.println(body); 
                System.out.println("\n------------------------------\n"); 
            }
        } catch (IOException ex) {
            Logger.getLogger(MboxLoader.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    private static String safeGet(String str, String delimiter) {
        try {
            return str.split(delimiter)[1].split("\\n")[0];
        } catch (Exception ignore) {
            return "";
        }
    }

    private static String getPreferredBody(String email, String boundary) {
        int index = 1;
        String toReturn = "";
        String[] splits = email.split(BOUNDARY_START + boundary);
        for (int i = index; i < splits.length; i++) {
            String message = splits[i];
            if (toReturn.isEmpty()) {
                toReturn = message;
            }
            
            if (message.contains(TEXT_PLAIN)) {
                toReturn = message;
                return toReturn;
            }
            
        }
        return toReturn;
    }

}
