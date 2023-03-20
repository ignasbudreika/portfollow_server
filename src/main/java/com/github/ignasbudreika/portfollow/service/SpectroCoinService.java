package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.SpectroCoinConnectionDTO;
import com.github.ignasbudreika.portfollow.enums.SpectroCoinConnectionStatus;
import com.github.ignasbudreika.portfollow.external.client.SpectroCoinClient;
import com.github.ignasbudreika.portfollow.model.SpectroCoinConnection;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.SpectroCoinConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;

@Service
public class SpectroCoinService {
    @Autowired
    private SpectroCoinConnectionRepository spectroCoinConnectionRepository;

    @Autowired
    private SpectroCoinClient spectroCoinClient;

    public void addConnection(SpectroCoinConnectionDTO connectionDTO, User user) throws Exception {
        if (spectroCoinConnectionRepository.findByClientIdAndUserId(connectionDTO.getClientId(), user.getId()) != null) {
            throw new EntityExistsException(String.format(
                    "SpectroCoin connection for user: %s and clientId: %s already exists", user.getId(), connectionDTO.getClientId()
            ));
        }

        if (!spectroCoinClient.credentialsAreValid(connectionDTO.getClientId(), connectionDTO.getClientSecret())) {
            throw new Exception(String.format("invalid SpectroCoin wallet API credentials for user: %s", user.getId()));
        }

        SpectroCoinConnection connection = SpectroCoinConnection.builder()
                .clientId(connectionDTO.getClientId())
                .clientSecret(connectionDTO.getClientSecret())
                .user(user)
                .status(SpectroCoinConnectionStatus.ACTIVE).build();

        spectroCoinConnectionRepository.save(connection);
    }

    @Transactional
    public void removeConnection(String id) {
        SpectroCoinConnection connection = spectroCoinConnectionRepository.findById(id).orElseThrow();

        connection.setStatus(SpectroCoinConnectionStatus.INACTIVE);
        connection.setClientSecret(null);

        spectroCoinConnectionRepository.save(connection);
    }

    @Transactional
    public void invalidateConnection(String id) {
        SpectroCoinConnection connection = spectroCoinConnectionRepository.findById(id).orElseThrow();

        connection.setStatus(SpectroCoinConnectionStatus.INVALID);
        connection.setClientSecret(null);

        spectroCoinConnectionRepository.save(connection);
    }
}
