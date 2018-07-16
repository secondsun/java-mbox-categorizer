package net.saga.java.mbox.ajug.persistence;

import org.hibernate.Session;

/**
 *
 * @author secon
 */
@FunctionalInterface
public interface HibernateAction {

    public void exec(Session session);
    
}
