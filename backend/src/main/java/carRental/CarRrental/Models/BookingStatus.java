package carRental.CarRrental.Models;

public enum BookingStatus {

    PENDING_PAYMENT,            // booking created
    // waiting for payment

    PAYMENT_SUCCESS,            // payment received
    // processing next step

    VERIFICATION_PENDING,       // SELF_DRIVE only
    // admin verifying
    // driving license

    DRIVER_ASSIGNMENT_PENDING,  // WITH_DRIVER only
    // no driver available
    // admin assigns manually

    CONFIRMED,                  // everything verified
    // ready for trip
    // car is BOOKED

    IN_PROGRESS,                // 👈 NEW
    // driver started trip
    // car is moving

    INSPECTION_PENDING,         // 👈 NEW
    // trip ended
    // admin inspecting car
    // before next booking

    COMPLETED,                  // car inspected ✅
    // trip fully done
    // rating can be given

    REJECTED,                   // license rejected
    // or fraud detected
    // refund initiated

    CANCELLED                   // user cancelled
    // refund based on
    // cancellation policy
}