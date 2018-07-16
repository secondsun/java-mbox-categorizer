package net.saga.java.mbox.ajug.persistence;

import java.util.List;
import net.saga.java.mbox.ajug.vo.Classification;
import net.saga.java.mbox.ajug.vo.EmailMessage;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 *
 * @author secon
 */
public class HibernateModule implements AutoCloseable {

    private SessionFactory sessionFactory;

    public void open() throws Exception {
        // A SessionFactory is set up once for an application!
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                .build();
        try {
            sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
            sessionFactory.getMetamodel();
        } catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
            // so destroy it manually.
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    public void query(HibernateAction action) {
        try (Session session = sessionFactory.openSession()) {
            action.exec(session);
        }
    }

    public void update(HibernateAction action) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            action.exec(session);
            session.getTransaction().commit();
        }
    }

    public boolean hasMessages() {
        try (Session session = sessionFactory.openSession()) {
            return !session.createQuery("from EmailMessage").list().isEmpty();
        }
    }

    @Override
    public void close() throws Exception {
        sessionFactory.close();
    }

    public void saveEmails(List<EmailMessage> emails) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                emails.forEach((email) -> session.save(email));
            } finally {
                if (tx != null) {
                    tx.commit();
                }
            }

        } 
    }

    public List<Classification> getCategories() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Classification c", Classification.class).getResultList();
        }
    }

    public void addCategory(String newCategory) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                Classification classification = new Classification();
                classification.setClassification(newCategory);
                session.save(classification);
            } finally {
                if (tx != null) {
                    tx.commit();
                }
            }

        } 
    }

    public List<EmailMessage> getMails() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from EmailMessage e order by e.send_date desc", EmailMessage.class).getResultList();
        }    }

    public void updateEmail(EmailMessage email) {
         try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.update(email);
            } finally {
                if (tx != null) {
                    tx.commit();
                }
            }

        } 
    }

}
