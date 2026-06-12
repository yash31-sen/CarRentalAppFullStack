package carRental.CarRrental.Models;

public enum TripType {

    ROUND_TRIP,     // pickup city = drop city
    // car returns to same location
    // no repositioning needed
    // no extra fee

    ONE_WAY         // pickup city != drop city
    // car ends up in different city
    // repositioning may be needed
    // one way fee charged
}