package carRental.CarRrental.Models;

public enum BookingType {
    SELF_DRIVE,     // user drives themselves
    // requires driving license
    // goes to VERIFICATION_PENDING

    WITH_DRIVER     // company driver assigned
    // no license needed from user
    // driver auto assigned by city
}