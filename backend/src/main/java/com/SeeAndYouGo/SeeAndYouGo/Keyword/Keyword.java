package com.SeeAndYouGo.SeeAndYouGo.Keyword;

import lombok.Getter;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Getter
public class Keyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword;

    @OneToMany(mappedBy = "keyword")
    private List<UserKeyword> userKeywords = new ArrayList<>();
}