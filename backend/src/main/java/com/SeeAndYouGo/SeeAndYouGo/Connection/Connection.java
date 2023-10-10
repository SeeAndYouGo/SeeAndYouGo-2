package com.SeeAndYouGo.SeeAndYouGo.Connection;

import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class Connection {
    @Id @GeneratedValue
    @Column(name = "connection_id")
    private Long id;

    private Integer connected;

    private String time;
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    private String data;

    private String name;
}
