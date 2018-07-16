/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.saga.java.mbox.ajug;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.saga.java.mbox.ajug.persistence.HibernateModule;
import net.saga.java.mbox.ajug.vo.EmailMessage;
import org.apache.james.mime4j.mboxiterator.CharBufferWrapper;
import org.apache.james.mime4j.mboxiterator.MboxIterator;

/**
 *
 * @author summers
 */
class MboxLoader {

    private static final CharsetEncoder ENCODER = Charset.forName("UTF-8").newEncoder();

    private static final String TO = "\n\\s*To:";
    private static final String FROM = "\n\\s*From:";
    private static final String DATE = "\n\\s*Date:";
    private static final String SUBJECT = "\n\\s*Subject:";
    private static final String CONTENT_TYPE = "\n\\s*Content-Type:";
    private static final String BOUNDARY = "boundary=";
    private static final String BOUNDARY_START = "--";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String TEXT_HTML = "text/html";

    static void loadMbox(File mbox, HibernateModule hibernate) {
        List<EmailMessage> emails = new ArrayList<>(100);
        int index  = 0;
        int empties = 0;

        try {
            for (CharBufferWrapper message : MboxIterator.fromFile(mbox).charset(ENCODER.charset()).build()) {
                String str = message.toString().replace("\r\n", "\n");
                String to = safeGet(str, TO);
                String from = safeGet(str, FROM);
                String date = safeGet(str, DATE);
                String subject = safeGet(str, SUBJECT);
                String contentType = safeGet(str, CONTENT_TYPE);
                String boundary = safeGet(contentType, BOUNDARY);

                if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
                    boundary = boundary.replace("\"", "");
                }
                
                String body = getPreferredBody(str, boundary);

                if (body.contains("Content-Transfer-Encoding: base64")) {
                    try {
                        String newBody = body.split("base64")[1].trim();
                        
                        body = new String(Base64.getMimeDecoder().decode(newBody), "UTF-8");
                    } catch(Exception swallow){
                        System.out.println(swallow.getMessage());
                    }
                }

                if (!body.isEmpty()) {
                    EmailMessage email = new EmailMessage();
                    email.setBody(body);
                    email.setEmail_sender(from);
                    email.setEmail_to(to);
                    email.setSend_date(date);
                    email.setSubject(subject);

                    emails.add(email);
                    if (emails.size() > 99) {
                        hibernate.saveEmails(emails);
                        emails.clear();
                    }

                    //System.out.println(index + ":" + subject);
                } else {
                    empties++;
                }
                index++;
            }
        } catch (IOException ex) {
            Logger.getLogger(MboxLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        hibernate.saveEmails(emails);
        System.out.println(index + " emails found and " + empties + " were skipped.");
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
