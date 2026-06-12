package carRental.CarRrental.Models;

public enum CarStatus {

    AVAILABLE,              // ready to be booked

    BOOKED,                 // currently on a trip

    NEEDS_REPOSITIONING,    // one way trip ended,
    // car is in different city
    // waiting for next booking
    // or return to home city

    MAINTENANCE,            // car is in service center
    // not available for booking

    PENDING_INSPECTION,     // trip just ended
    // admin needs to inspect
    // before making available again

    DISABLED                // permanently removed
    // from fleet
}