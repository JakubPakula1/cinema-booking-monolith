package io.github.jakubpakula1.cinema.repository;

import io.github.jakubpakula1.cinema.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
