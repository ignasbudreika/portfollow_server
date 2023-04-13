package com.github.ignasbudreika.portfollow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String description;
    private boolean published;
    private boolean hiddenValue;
    private boolean currencyEur;
    private String allowedUsers;
    @OneToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;
    @OneToMany(mappedBy = "id")
    private Set<PortfolioHistory> history = new HashSet<>();
}
