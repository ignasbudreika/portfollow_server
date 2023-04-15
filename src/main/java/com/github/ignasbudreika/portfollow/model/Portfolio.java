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
public class Portfolio {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String title;
    private String description;
    @Column(name = "published")
    private boolean published;
    @Column(name = "hidden_value")
    private boolean hiddenValue;
    @Column(name = "currency_eur")
    private boolean currencyEur;
    @Column(name = "allowed_users")
    private String allowedUsers;
    @OneToOne
    @JoinColumn(name="user_id", nullable=false, unique = true)
    private User user;
}
