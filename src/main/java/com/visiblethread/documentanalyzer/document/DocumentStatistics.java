package com.visiblethread.documentanalyzer.document;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class DocumentStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "document_id")
    private Document document;
    
    @Column(name = "word_count")
    private int wordCount;
    
    @Column(name = "shortest_word")
    private String shortestWord;
    
    @Column(name = "longest_word")
    private String longestWord;
    
    @OneToMany(mappedBy = "documentStatistics", cascade = CascadeType.ALL)
    private List<WordFrequency> wordFrequencies = new ArrayList<>();
}