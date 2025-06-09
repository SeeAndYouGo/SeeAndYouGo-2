package com.SeeAndYouGo.SeeAndYouGo.visitor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class VisitorCount {

    public static VisitorCount from(int count, boolean isTotal) {
        return new VisitorCount(count, isTotal);
    }

    private VisitorCount(int count, boolean isTotal) {
        this.count = count;
        this.isTotal = isTotal;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visitor_count_id")
    private Long id;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private int count;

    private boolean isTotal;

    public void updateCount(int cnt) {
        this.count = cnt;
        this.createdAt = LocalDateTime.now();
    }
}