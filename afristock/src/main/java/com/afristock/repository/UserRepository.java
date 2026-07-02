package com.afristock.repository;

import com.afristock.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.company WHERE u.email = :email")
    public Optional<User> findByEmail(String email);
    // LEFT JOIN : le Super-Administrateur n'a pas d'entreprise, il doit quand même être retourné.
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.company WHERE u.id = :id")
    User findByIdWithCompany(Long id);
}
