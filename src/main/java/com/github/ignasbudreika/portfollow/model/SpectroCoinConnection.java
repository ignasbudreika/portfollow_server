package com.github.ignasbudreika.portfollow.model;

import com.github.ignasbudreika.portfollow.component.Encrypt;
import com.github.ignasbudreika.portfollow.enums.SpectroCoinConnectionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "spectrocoin_connection")
public class SpectroCoinConnection {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    @Convert(converter = Encrypt.class)
    @Column(name = "client_id", nullable = false)
    private String clientId;
    @Convert(converter = Encrypt.class)
    @Column(name = "client_secret")
    private String clientSecret;
    @Column(name = "last_fetched")
    private LocalDateTime lastFetched;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private SpectroCoinConnectionStatus status;
}
