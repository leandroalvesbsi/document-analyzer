package com.visiblethread.documentanalyzer.document;

import com.visiblethread.documentanalyzer.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "document")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_email", nullable = false)
    private User user;

    @CreatedDate
    @Column(name = "upload_timestamp")
    private LocalDateTime uploadTimestamp;

    @Column(name = "s3_object_key", nullable = false)
    private String s3ObjectKey;

    @OneToOne(mappedBy = "document", cascade = CascadeType.ALL)
    private DocumentStatistics statistics;

}