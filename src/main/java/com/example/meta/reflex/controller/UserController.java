package com.example.meta.reflex.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.reflex.model.User;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 1L;

    // POST: Create a new user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        user.setId(currentId++);
        users.put(user.getId(), user);
        return ResponseEntity.ok(user);
    }

    // GET with @RequestBody (not recommended by REST standards, but technically possible)
    @GetMapping("/search")
    public ResponseEntity<User> getUserByRequestBody(@RequestBody User user) {
        return users.values().stream()
                .filter(u -> u.getName().equalsIgnoreCase(user.getName()))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT: Replace user completely
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        if (!users.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        user.setId(id);
        users.put(id, user);
        return ResponseEntity.ok(user);
    }

    // PATCH: Partially update user
    @PatchMapping("/{id}")
    public ResponseEntity<User> partiallyUpdateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        User user = users.get(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        updates.forEach((key, value) -> {
            switch (key) {
                case "name": user.setName((String) value); break;
                case "email": user.setEmail((String) value); break;
                // Add more fields as needed
            }
        });

        return ResponseEntity.ok(user);
    }

    // DELETE: Optional body (not typical, but supported)
    @DeleteMapping
    public ResponseEntity<String> deleteUserByRequestBody(@RequestBody User user) {
        Optional<Long> userId = users.entrySet().stream()
                .filter(entry -> entry.getValue().getName().equalsIgnoreCase(user.getName()))
                .map(Map.Entry::getKey)
                .findFirst();

        return userId.map(id -> {
            users.remove(id);
            return ResponseEntity.ok("User deleted with id: " + id);
        }).orElse(ResponseEntity.notFound().build());
    }
}