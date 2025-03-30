package com.visiblethread.documentanalyzer.document;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordFrequency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word")
    private String word;

    @Column(name = "frequency")
    private int frequency;

    @ManyToOne
    @JoinColumn(name = "document_statistics_id")
    private DocumentStatistics documentStatistics;

}