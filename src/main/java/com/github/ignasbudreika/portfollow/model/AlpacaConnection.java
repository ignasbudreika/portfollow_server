package com.github.ignasbudreika.portfollow.model;

import com.github.ignasbudreika.portfollow.component.Encrypt;
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
@Table(name = "alpaca_connection")
public class AlpacaConnection {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    @Convert(converter = Encrypt.class)
    @Column(name = "api_key", nullable = false)
    private String apiKey;
    @Convert(converter = Encrypt.class)
    @Column(name = "secret")
    private String secret;
    @UpdateTimestamp
    @Column(name = "last_fetched")
    private LocalDateTime lastFetched;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private ConnectionStatus status;
}
