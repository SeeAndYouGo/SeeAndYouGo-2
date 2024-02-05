package com.SeeAndYouGo.SeeAndYouGo.Keyword;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Getter
@NoArgsConstructor
public class Keyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "keyword")
    private List<UserKeyword> userKeywords = new ArrayList<>();

    public Keyword(String name) {
        this.name = name;
    }
}