package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class UserService {
    
	@Autowired
    private  UserRepository repo;
	@Autowired
    private  RedisTemplate<String, Object> redisTemplate;

    public Page<User> getUsers(String city, int page, int size) {

        String key = "users:" + city + ":" + page + ":" + size;

        // 🔥 Check cache
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (Page<User>) cached;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<User> result;

        if (city == null || city.isEmpty()) {
            result = repo.findAll(pageable);
        } else {
            result = repo.findByCityContainingIgnoreCase(city, pageable);
        }

        // 🔥 Store in Redis (cache for 60 sec)
        redisTemplate.opsForValue().set(key, result, Duration.ofSeconds(60));

        return result;
    }

    public User save(User user) {
        return repo.save(user);
    }
}