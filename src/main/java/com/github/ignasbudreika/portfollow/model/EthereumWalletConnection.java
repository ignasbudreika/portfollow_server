package com.github.ignasbudreika.portfollow.model;

import com.github.ignasbudreika.portfollow.enums.ConnectionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ethereum_wallet_connection")
public class EthereumWalletConnection {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    @Column(nullable = false)
    private String address;
    @Column(nullable = false)
    private ConnectionStatus status;
    @UpdateTimestamp
    @Column(name = "last_fetched")
    private LocalDateTime lastFetched;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
