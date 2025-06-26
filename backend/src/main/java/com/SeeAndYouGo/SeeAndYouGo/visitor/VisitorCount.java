package com.SeeAndYouGo.SeeAndYouGo.visitor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
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

    private LocalDate createdAt;

    private int count;

    private boolean isTotal;

    public static VisitorCount from(int count, LocalDate createAt, boolean isTotal) {
        return new VisitorCount(count, createAt, isTotal);
    }

    public VisitorCount(int count, LocalDate createdAt, boolean isTotal) {
        this.count = count;
        this.createdAt = createdAt;
        this.isTotal = isTotal;
    }

    public void updateCount(int resultCount) {
        this.count = resultCount;
    }
}