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
                "SELECT connected FROM Connected connected " +
                        "WHERE connected.name LIKE CONCAT('%', :name, '%') " +
                        "AND connected.time = (SELECT MAX(c2.time) FROM Connected c2 " +
                        "WHERE c2.name LIKE CONCAT('%', :name, '%'))",
                Connection.class
        );
        query.setParameter("name", restaurantName);

        return query.getSingleResult();
    }

    public Long countNumberOfData(){
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(*) FROM Connected",
                Long.class
        );
        return query.getSingleResult();
    }
}