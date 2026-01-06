package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.model.Room;
import io.github.jakubpakula1.cinema.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    void testGetAllRooms_Success() {
        // given
        Room room = new Room();
        room.setId(1L);
        room.setName("Room A");

        when(roomRepository.findAll()).thenReturn(List.of(room));

        // when
        List<Room> result = roomService.getAllRooms();

        // then
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .extracting(Room::getId)
                .containsExactly(1L);

        verify(roomRepository).findAll();
    }
}