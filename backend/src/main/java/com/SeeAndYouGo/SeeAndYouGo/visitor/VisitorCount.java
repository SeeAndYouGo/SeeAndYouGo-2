package com.SeeAndYouGo.SeeAndYouGo.visitor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class VisitorCount {

    public static VisitorCount from(int count, boolean isTotal) {
        if (isTotal)
            return new VisitorCount(count, isTotal, LocalDateTime.now().minusDays(1));
        else
            return new VisitorCount(count, isTotal, LocalDateTime.now());
    }

    private VisitorCount(int count, boolean isTotal, LocalDateTime createdAt) {
        this.count = count;
        this.isTotal = isTotal;
        this.createdAt = createdAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visitor_count_id")
    private Long id;

    private LocalDateTime createdAt;

    private int count;

    private boolean isTotal;

    public int updateCount(int cnt) {
        this.count = cnt;
        return this.count;
    }
}