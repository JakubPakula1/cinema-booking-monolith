package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.model.Room;
import io.github.jakubpakula1.cinema.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }
}
