package com.SeeAndYouGo.SeeAndYouGo.Connection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

@Repository
@RequiredArgsConstructor
public class ConnectionRepository {
    private final EntityManager em;

    public void save(Connection connection){em.persist(connection);}

    public Connection findRecent(String restaurantName){
        TypedQuery<Connection> query = em.createQuery(
                "SELECT c FROM Connection c " +
                        "WHERE c.time = (SELECT MAX(c2.time) FROM Connection c2 " +
                        "WHERE c2.restaurant.name = :name) " +
                        "AND c.restaurant.name = :name",
                Connection.class
        );
        query.setParameter("name", restaurantName);
        return query.getSingleResult();
    }

    public String findRecentTime(){
//        String res = "NULL";
        TypedQuery<String> query = em.createQuery(
                "SELECT MAX(ct2.time) FROM Connection ct2",
                String.class
        );

        return query.getSingleResult();
    }


    public Long countNumberOfData(){
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(*) FROM Connection",
                Long.class
        );
        return query.getSingleResult();
    }
}