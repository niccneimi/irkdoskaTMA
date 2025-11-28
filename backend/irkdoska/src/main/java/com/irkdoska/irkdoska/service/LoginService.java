package com.irkdoska.irkdoska.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.irkdoska.irkdoska.repository.UserRepository;
import com.irkdoska.irkdoska.model.User;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final UserRepository userRepository;

    @Transactional
    public void login(Long telegramId, String username) {
        userRepository.findByTelegramId(telegramId).orElseGet(
            () -> userRepository.save(new User(telegramId, username))
        );
    }
}
