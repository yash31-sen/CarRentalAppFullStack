package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.CarClass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarPricingRequest {

    private CarClass carClass;          // which class this rule is for

    private double baseFare;            // flat booking fee

    private double perDayRate;          // default daily rate

    private double minPerDayRate;       // minimum daily rate

    private double maxPerDayRate;       // maximum daily rate

    private double freeKmPerDay;        // free km per day

    private double extraKmRate;         // rate per extra km
}