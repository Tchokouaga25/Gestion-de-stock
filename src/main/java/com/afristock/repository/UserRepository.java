package com.afristock.repository;

import com.afristock.model.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(@NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email);
    public Optional<User> findByEmail(String username);
    public abstract User save(User user);
    @Query("SELECT u FROM User u JOIN FETCH u.company WHERE u.id = :id")
    User findByIdWithCompany(Long id);
}
