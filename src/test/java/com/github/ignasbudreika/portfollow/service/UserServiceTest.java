package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;

class UserServiceTest {
    private static final String USER_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final String GOOGLE_ID = "11111111111111111";
    private static final String USER_EMAIL = "test@e.mail";
    private static final String USER_USERNAME = "John Doe";
    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserService target = new UserService(userRepository);

    @Test
    void shouldCreateUser() {
        User user = User.builder()
                .id(USER_ID)
                .googleId(GOOGLE_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.existsByGoogleId(user.getGoogleId())).thenReturn(false);


        target.createUser(user);


        verify(userRepository).existsByGoogleId(user.getGoogleId());
        verify(userRepository).existsByEmail(user.getEmail());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        Assertions.assertEquals(user.getGoogleId(), captor.getValue().getGoogleId());
        Assertions.assertEquals(user.getEmail(), captor.getValue().getEmail());
        Assertions.assertEquals(user.getUsername(), captor.getValue().getUsername());
    }

    @Test
    void shouldThrowEntityExistsException_whenUserExists() {
        User user = User.builder()
                .id(USER_ID)
                .googleId(GOOGLE_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);


        Assertions.assertThrows(EntityExistsException.class, () -> target.createUser(user));


        verify(userRepository, never()).save(any(User.class));
    }
}
