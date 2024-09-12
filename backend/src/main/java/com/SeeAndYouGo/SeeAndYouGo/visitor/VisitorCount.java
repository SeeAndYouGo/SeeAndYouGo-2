package com.SeeAndYouGo.SeeAndYouGo.visitor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class VisitorCount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visitor_count_id")
    private Long id;

    @CreatedDate
    private LocalDateTime createdAt;

    private int count;

    public VisitorCount(int count) {
        this.count = count;
    }
}
