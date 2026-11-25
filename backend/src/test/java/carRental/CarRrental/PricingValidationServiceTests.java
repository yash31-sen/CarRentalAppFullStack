package carRental.CarRrental;

import carRental.CarRrental.Models.*;
import carRental.CarRrental.Repositories.CarPricingRepository;
import carRental.CarRrental.Repositories.CarRepository;
import carRental.CarRrental.Services.PricingValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingValidationServiceTests {

    @Mock
    private CarPricingRepository carPricingRepository;

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private PricingValidationService pricingValidationService;

    @Test
    void validateHostVehiclePrice_rejectsBelowMinimum() {
        CarPricing hatchback = CarPricing.builder()
                .carClass(CarClass.HATCHBACK)
                .minPerDayRate(1000.0)
                .maxPerDayRate(2000.0)
                .active(true)
                .build();
        when(carPricingRepository.findFirstByCarClassAndActiveTrue(CarClass.HATCHBACK))
                .thenReturn(Optional.of(hatchback));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                pricingValidationService.validateHostVehiclePrice(CarClass.HATCHBACK, new BigDecimal("700")));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Minimum permitted daily rental price for HATCHBACK is ₹1,000."));
    }

    @Test
    void validateHostVehiclePrice_rejectsAboveMaximum() {
        CarPricing hatchback = CarPricing.builder()
                .carClass(CarClass.HATCHBACK)
                .minPerDayRate(1000.0)
                .maxPerDayRate(2000.0)
                .active(true)
                .build();
        when(carPricingRepository.findFirstByCarClassAndActiveTrue(CarClass.HATCHBACK))
                .thenReturn(Optional.of(hatchback));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                pricingValidationService.validateHostVehiclePrice(CarClass.HATCHBACK, new BigDecimal("2500")));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Maximum permitted daily rental price for HATCHBACK is ₹2,000."));
    }

    @Test
    void validateHostVehiclePrice_acceptsWithinRange() {
        CarPricing hatchback = CarPricing.builder()
                .carClass(CarClass.HATCHBACK)
                .minPerDayRate(1000.0)
                .maxPerDayRate(2000.0)
                .active(true)
                .build();
        when(carPricingRepository.findFirstByCarClassAndActiveTrue(CarClass.HATCHBACK))
                .thenReturn(Optional.of(hatchback));

        CarPricing result = pricingValidationService.validateHostVehiclePrice(CarClass.HATCHBACK, new BigDecimal("1500"));
        assertNotNull(result);
        assertEquals(CarClass.HATCHBACK, result.getCarClass());
    }

    @Test
    void calculateCommissionSnapshot_computesHostShareAndPlatformCommission() {
        CarPricing sedan = CarPricing.builder()
                .carClass(CarClass.SEDAN)
                .platformCommissionPercent(new BigDecimal("20.00"))
                .minPlatformFee(BigDecimal.ZERO)
                .build();

        PricingValidationService.CommissionSnapshot snapshot =
                pricingValidationService.calculateCommissionSnapshot(
                        new BigDecimal("2000.00"), sedan, new BigDecimal("99.00"));

        assertEquals(new BigDecimal("2000.00"), snapshot.getGrossAmount());
        assertEquals(new BigDecimal("20.00"), snapshot.getCommissionPercent());
        assertEquals(new BigDecimal("400.00"), snapshot.getCommissionAmount());
        assertEquals(new BigDecimal("1600.00"), snapshot.getHostEarningsAmount());
        assertEquals(new BigDecimal("99.00"), snapshot.getPlatformFeeAmount());
    }

    @Test
    void flagOutOfRangeVehicles_flagsCarsWithoutModifyingHostPrice() {
        CarPricing suv = CarPricing.builder()
                .carClass(CarClass.SUV)
                .minPerDayRate(2500.0)
                .maxPerDayRate(5000.0)
                .build();

        Car tooCheap = Car.builder().id(1L).carClass(CarClass.SUV).pricePerDay(2000.0).pricingOutOfRange(false).build();
        Car inRange = Car.builder().id(2L).carClass(CarClass.SUV).pricePerDay(3500.0).pricingOutOfRange(false).build();
        when(carRepository.findByCarClass(CarClass.SUV)).thenReturn(List.of(tooCheap, inRange));

        pricingValidationService.flagOutOfRangeVehicles(suv);

        assertTrue(tooCheap.getPricingOutOfRange());
        assertEquals(2000.0, tooCheap.getPricePerDay()); // Host price NOT silently modified
        assertFalse(inRange.getPricingOutOfRange());
        verify(carRepository, times(1)).save(tooCheap);
    }
}
