package carRental.CarRrental.Dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentSuccessRequest {
    private String paymentRef; // txn id from gateway
}
