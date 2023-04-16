package com.github.ignasbudreika.portfollow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String comment;
    @ManyToOne
    @JoinColumn(name="portfolio_id")
    private Portfolio portfolio;
    @OneToOne
    @JoinColumn(name="user_id")
    private User user;
}
