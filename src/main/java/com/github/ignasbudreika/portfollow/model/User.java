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
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String googleId;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "id")
    private Set<Investment> holdings = new HashSet<>();
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "id")
    private Set<SpectroCoinConnection> spectroCoinConnections = new HashSet<>();
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "id")
    private Set<EthereumWalletConnection> ethereumWalletConnections = new HashSet<>();
}
