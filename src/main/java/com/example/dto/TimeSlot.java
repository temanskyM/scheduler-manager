package com.example.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TimeSlot {
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;

    public TimeSlot(LocalDateTime dateStart, LocalDateTime dateEnd) {
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
    }

    public boolean overlaps(TimeSlot other) {
        // Check for exact start time match
        if (this.dateStart.equals(other.dateStart)) {
            return true;
        }
        // Check for overlap
        return !this.dateStart.isAfter(other.dateEnd) && !other.dateStart.isAfter(this.dateEnd);
    }
} 