package autodviratis.hibernate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

import java.util.List;
import java.util.Optional;

public class GenericHibernate {

    private final EntityManagerFactory entityManagerFactory;

    public GenericHibernate(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public <T> void create(T entity) {
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
        } finally {
            close(em);
        }
    }

    public <T> List<T> getAllRecords(Class<T> entityClass) {
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> query = cb.createQuery(entityClass);
            query.select(query.from(entityClass));
            return em.createQuery(query).getResultList();
        } finally {
            close(em);
        }
    }

    public <T> Optional<T> getById(Class<T> entityClass, Object id) {
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            return Optional.ofNullable(em.find(entityClass, id));
        } finally {
            close(em);
        }
    }

    public <T> void update(T entity) {
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            em.getTransaction().begin();
            em.merge(entity);
            em.getTransaction().commit();
        } finally {
            close(em);
        }
    }

    public <T> void delete(Class<T> entityClass, Object id) {
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            em.getTransaction().begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
            em.getTransaction().commit();
        } finally {
            close(em);
        }
    }

    private void close(EntityManager em) {
        if (em == null) {
            return;
        }
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.close();
    }
}
