package carRental.CarRrental.Models;

public enum BookingStatus {
    PENDING_PAYMENT,
    PAYMENT_SUCCESS,
    VERIFICATION_PENDING,
    DRIVER_ASSIGNMENT_PENDING,
    CONFIRMED,
    REJECTED,
    CANCELLED,
    COMPLETED
}
