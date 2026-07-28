package com.cinema.ebooking.dto;

import java.util.List;

public record SeatMapResponse(
        Long showtimeId,
        Long showroomId,
        String showroomName,
        List<SeatResponse> seats
) {
}
