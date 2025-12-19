package io.github.jakubpakula1.cinema.repository;

import io.github.jakubpakula1.cinema.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
