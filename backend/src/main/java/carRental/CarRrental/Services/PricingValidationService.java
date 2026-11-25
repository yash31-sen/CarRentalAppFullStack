package carRental.CarRrental.Services;

import carRental.CarRrental.Models.Car;
import carRental.CarRrental.Models.CarClass;
import carRental.CarRrental.Models.CarPricing;
import carRental.CarRrental.Repositories.CarPricingRepository;
import carRental.CarRrental.Repositories.CarRepository;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingValidationService {

    private final CarPricingRepository carPricingRepository;
    private final CarRepository carRepository;

    /**
     * Validates a Host's requested daily rental price against the active CarPricing rule
     * for the vehicle's category.
     */
    public CarPricing validateHostVehiclePrice(CarClass carClass, BigDecimal requestedDailyPrice) {
        if (carClass == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Car category is required");
        }
        if (requestedDailyPrice == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Daily rental price is required");
        }

        CarPricing pricing = carPricingRepository.findFirstByCarClassAndActiveTrue(carClass)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No active pricing configuration found for " + carClass + ". Contact Super Admin."
                ));

        BigDecimal min = BigDecimal.valueOf(pricing.getMinPerDayRate()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal max = BigDecimal.valueOf(pricing.getMaxPerDayRate()).setScale(2, RoundingMode.HALF_UP);

        if (requestedDailyPrice.compareTo(min) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum permitted daily rental price for " + carClass + " is ₹" + formatMoney(min) + "."
            );
        }

        if (requestedDailyPrice.compareTo(max) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maximum permitted daily rental price for " + carClass + " is ₹" + formatMoney(max) + "."
            );
        }

        return pricing;
    }

    /**
     * Calculates immutable commission snapshot using BigDecimal.
     */
    public CommissionSnapshot calculateCommissionSnapshot(BigDecimal grossRentalAmount,
                                                          CarPricing pricing,
                                                          BigDecimal platformServiceFee) {
        if (grossRentalAmount == null) {
            grossRentalAmount = BigDecimal.ZERO;
        }
        grossRentalAmount = grossRentalAmount.setScale(2, RoundingMode.HALF_UP);

        BigDecimal commissionPercent = (pricing != null && pricing.getPlatformCommissionPercent() != null)
                ? pricing.getPlatformCommissionPercent()
                : new BigDecimal("20.00");

        BigDecimal commissionAmount = grossRentalAmount
                .multiply(commissionPercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        if (pricing != null && pricing.getMinPlatformFee() != null
                && commissionAmount.compareTo(pricing.getMinPlatformFee()) < 0) {
            commissionAmount = pricing.getMinPlatformFee().setScale(2, RoundingMode.HALF_UP);
        }
        if (pricing != null && pricing.getMaxPlatformFee() != null
                && pricing.getMaxPlatformFee().compareTo(BigDecimal.ZERO) > 0
                && commissionAmount.compareTo(pricing.getMaxPlatformFee()) > 0) {
            commissionAmount = pricing.getMaxPlatformFee().setScale(2, RoundingMode.HALF_UP);
        }

        // Ensure commission does not exceed gross amount
        if (commissionAmount.compareTo(grossRentalAmount) > 0) {
            commissionAmount = grossRentalAmount;
        }

        BigDecimal feeAmount = platformServiceFee != null
                ? platformServiceFee.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal hostEarnings = grossRentalAmount.subtract(commissionAmount).setScale(2, RoundingMode.HALF_UP);

        return CommissionSnapshot.builder()
                .grossAmount(grossRentalAmount)
                .commissionPercent(commissionPercent)
                .commissionAmount(commissionAmount)
                .platformFeeAmount(feeAmount)
                .hostEarningsAmount(hostEarnings)
                .build();
    }

    /**
     * Flags existing vehicles whose pricePerDay falls outside newly updated category limits,
     * without silently modifying Host prices.
     */
    @Transactional
    public void flagOutOfRangeVehicles(CarPricing pricing) {
        List<Car> cars = carRepository.findByCarClass(pricing.getCarClass());
        for (Car car : cars) {
            BigDecimal price = BigDecimal.valueOf(car.getPricePerDay());
            BigDecimal min = BigDecimal.valueOf(pricing.getMinPerDayRate());
            BigDecimal max = BigDecimal.valueOf(pricing.getMaxPerDayRate());
            boolean outOfRange = price.compareTo(min) < 0 || price.compareTo(max) > 0;
            if (!Boolean.valueOf(outOfRange).equals(car.getPricingOutOfRange())) {
                car.setPricingOutOfRange(outOfRange);
                carRepository.save(car);
            }
        }
    }

    private String formatMoney(BigDecimal val) {
        if (val.stripTrailingZeros().scale() <= 0) {
            return String.format("%,d", val.longValue());
        }
        return val.toPlainString();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommissionSnapshot {
        private BigDecimal grossAmount;
        private BigDecimal commissionPercent;
        private BigDecimal commissionAmount;
        private BigDecimal platformFeeAmount;
        private BigDecimal hostEarningsAmount;
    }
}
