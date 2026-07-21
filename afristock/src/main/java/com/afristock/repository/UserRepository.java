package com.afristock.repository;

import com.afristock.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.company LEFT JOIN FETCH u.site WHERE u.email = :email")
    public Optional<User> findByEmail(String email);
    // LEFT JOIN : le Super-Administrateur n'a pas d'entreprise, il doit quand même être retourné.
    // u.site est aussi chargé ici (comme u.company) pour éviter une LazyInitializationException
    // quand le principal Spring Security (chargé via findByEmail au login) est réutilisé sur des
    // requêtes ultérieures dont la session Hibernate d'origine est fermée.
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.company LEFT JOIN FETCH u.site WHERE u.id = :id")
    User findByIdWithCompany(Long id);
}
