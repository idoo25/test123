package parking.util;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for time-related operations in the parking system
 * Adapted for OCSF client-server architecture
 */
public class TimeUtils {
    
    public static final int SLOT_INTERVAL_MINUTES = 15;
    public static final double HOURS_PER_SLOT = 0.25;
    public static final int MAX_BOOKING_HOURS = 4;
    public static final int MAX_SLOTS_PER_BOOKING = 16; // 4 hours / 0.25 hours per slot
    
    /**
     * Round time down to nearest 15-minute slot
     * Example: 14:32 → 14:30, 09:07 → 09:00
     */
    public static LocalTime roundToNearestSlot(LocalTime time) {
        if (time == null) return null;
        
        int minutes = time.getMinute();
        int roundedMinutes = (minutes / SLOT_INTERVAL_MINUTES) * SLOT_INTERVAL_MINUTES;
        return LocalTime.of(time.getHour(), roundedMinutes);
    }
    
    /**
     * Round time up to next 15-minute slot
     * Example: 14:32 → 14:45, 09:07 → 09:15
     */
    public static LocalTime roundUpToNextSlot(LocalTime time) {
        if (time == null) return null;
        
        int minutes = time.getMinute();
        if (minutes % SLOT_INTERVAL_MINUTES == 0) {
            return time; // Already on slot boundary
        }
        
        int roundedMinutes = ((minutes / SLOT_INTERVAL_MINUTES) + 1) * SLOT_INTERVAL_MINUTES;
        if (roundedMinutes >= 60) {
            return LocalTime.of((time.getHour() + 1) % 24, 0);
        }
        return LocalTime.of(time.getHour(), roundedMinutes);
    }
    
    /**
     * Check if time is on 15-minute boundary
     */
    public static boolean isValidTimeSlot(LocalTime time) {
        return time != null && time.getMinute() % SLOT_INTERVAL_MINUTES == 0;
    }
    
    /**
     * Get next 15-minute slot
     */
    public static LocalTime getNextSlot(LocalTime time) {
        if (time == null) return null;
        return time.plusMinutes(SLOT_INTERVAL_MINUTES);
    }
    
    /**
     * Get previous 15-minute slot
     */
    public static LocalTime getPreviousSlot(LocalTime time) {
        if (time == null) return null;
        return time.minusMinutes(SLOT_INTERVAL_MINUTES);
    }
    
    /**
     * Calculate number of 15-minute slots between two times
     */
    public static int calculateSlotsBetween(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) return 0;
        if (endTime.isBefore(startTime)) return 0;
        
        long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
        return (int) (minutes / SLOT_INTERVAL_MINUTES);
    }
    
    /**
     * Calculate duration in hours between two times
     */
    public static double calculateDurationHours(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) return 0.0;
        if (endTime.isBefore(startTime)) return 0.0;
        
        long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
        return minutes / 60.0;
    }
    
    /**
     * Generate list of all 15-minute time slots for a day
     */
    public static List<LocalTime> generateDayTimeSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = LocalTime.of(0, 0);
        
        for (int i = 0; i < 96; i++) { // 24 hours * 4 slots per hour = 96 slots
            slots.add(current);
            current = current.plusMinutes(SLOT_INTERVAL_MINUTES);
        }
        
        return slots;
    }
    
    /**
     * Generate time slots between start and end time
     */
    public static List<LocalTime> generateTimeSlotsBetween(LocalTime startTime, LocalTime endTime) {
        List<LocalTime> slots = new ArrayList<>();
        
        if (startTime == null || endTime == null || endTime.isBefore(startTime)) {
            return slots;
        }
        
        LocalTime current = roundToNearestSlot(startTime);
        LocalTime end = roundToNearestSlot(endTime);
        
        while (!current.isAfter(end)) {
            slots.add(current);
            current = getNextSlot(current);
            
            // Safety check to prevent infinite loop
            if (slots.size() > MAX_SLOTS_PER_BOOKING) {
                break;
            }
        }
        
        return slots;
    }
    
    /**
     * Check if a duration is within allowed limits (max 4 hours)
     */
    public static boolean isValidDuration(double hours) {
        return hours > 0 && hours <= MAX_BOOKING_HOURS;
    }
    
    /**
     * Check if a duration is within allowed limits (max 4 hours)
     */
    public static boolean isValidDuration(LocalTime startTime, LocalTime endTime) {
        double hours = calculateDurationHours(startTime, endTime);
        return isValidDuration(hours);
    }
    
    /**
     * Format duration as human readable string
     */
    public static String formatDuration(double hours) {
        if (hours >= 1) {
            return String.format("%.1f hours", hours);
        } else {
            int minutes = (int) (hours * 60);
            return String.format("%d minutes", minutes);
        }
    }
    
    /**
     * Format time slot for display
     */
    public static String formatTimeSlot(LocalTime time) {
        if (time == null) return "??:??";
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    /**
     * Format date for display
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "No date";
        return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
    
    /**
     * Format date and time range for display
     */
    public static String formatTimeRange(LocalDate date, LocalTime startTime, LocalTime endTime) {
        StringBuilder sb = new StringBuilder();
        
        if (date != null) {
            sb.append(formatDate(date)).append(" ");
        }
        
        sb.append(formatTimeSlot(startTime));
        sb.append(" - ");
        sb.append(formatTimeSlot(endTime));
        
        return sb.toString();
    }
    
    /**
     * Get current time rounded to current 15-minute slot
     */
    public static LocalTime getCurrentTimeSlot() {
        return roundToNearestSlot(LocalTime.now());
    }
    
    /**
     * Check if given time is in the past
     */
    public static boolean isInPast(LocalDate date, LocalTime time) {
        if (date == null || time == null) return false;
        
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        if (date.isBefore(today)) {
            return true;
        } else if (date.equals(today)) {
            return time.isBefore(now);
        } else {
            return false;
        }
    }
    
    /**
     * Validate booking time constraints
     */
    public static boolean isValidBookingTime(LocalDate date, LocalTime startTime, LocalTime endTime) {
        // Check if times are valid
        if (date == null || startTime == null || endTime == null) {
            return false;
        }
        
        // Check if start time is before end time
        if (!startTime.isBefore(endTime)) {
            return false;
        }
        
        // Check if times are on slot boundaries
        if (!isValidTimeSlot(startTime) || !isValidTimeSlot(endTime)) {
            return false;
        }
        
        // Check if duration is within limits
        if (!isValidDuration(startTime, endTime)) {
            return false;
        }
        
        // Check if booking is not in the past
        if (isInPast(date, startTime)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get a formatted string showing available time slots for the day
     */
    public static String getAvailableTimeSlotsDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available time slots (15-minute intervals):\n");
        
        LocalTime current = LocalTime.of(6, 0); // Start from 6:00 AM
        LocalTime end = LocalTime.of(22, 0);    // End at 10:00 PM
        
        int count = 0;
        while (!current.isAfter(end)) {
            sb.append(formatTimeSlot(current));
            
            count++;
            if (count % 8 == 0) {
                sb.append("\n");
            } else {
                sb.append("  ");
            }
            
            current = getNextSlot(current);
        }
        
        return sb.toString();
    }
    
    /**
     * Check if two time periods overlap
     */
    public static boolean timePeriodsOverlap(LocalTime start1, LocalTime end1, 
                                           LocalTime start2, LocalTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        
        return !(end1.isBefore(start2) || start1.isAfter(end2));
    }
    
    /**
     * Get time until a specific target time
     */
    public static String getTimeUntil(LocalTime targetTime) {
        if (targetTime == null) return "Unknown";
        
        LocalTime now = LocalTime.now();
        if (targetTime.isBefore(now)) {
            return "Time has passed";
        }
        
        long minutes = java.time.Duration.between(now, targetTime).toMinutes();
        
        if (minutes < 60) {
            return minutes + " minutes";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return hours + "h " + remainingMinutes + "m";
        }
    }
    
    /**
     * Convert minutes to hours and minutes display
     */
    public static String minutesToHoursAndMinutes(long totalMinutes) {
        if (totalMinutes < 60) {
            return totalMinutes + " minutes";
        }
        
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        
        if (minutes == 0) {
            return hours + " hour" + (hours != 1 ? "s" : "");
        } else {
            return hours + "h " + minutes + "m";
        }
    }
}