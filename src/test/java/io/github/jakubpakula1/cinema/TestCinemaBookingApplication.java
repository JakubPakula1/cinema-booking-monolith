package io.github.jakubpakula1.cinema;

import org.springframework.boot.SpringApplication;

public class TestCinemaBookingApplication {

	public static void main(String[] args) {
		SpringApplication.from(CinemaBookingApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
