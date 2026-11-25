    package carRental.CarRrental.Models;
    
    import jakarta.persistence.*;
    import lombok.*;

    import java.math.BigDecimal;
    
    @Entity
    @Table(name = "service_city")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class ServiceCity {
    
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
    
        // city name - must be unique
        @Column(nullable = false, unique = true)
        private String cityName;            // "Indore"
    
        // is this city currently active?
        @Column(nullable = false)
        @Builder.Default
        private boolean active = true;      // can be deactivated

        @Column(name = "booking_available")
        @Builder.Default
        private Boolean bookingAvailable = true;
    
        // which admin manages this city
        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "admin_id")
        private AppUser admin;              // Indore Admin
    
        // where cars park in this city
        @Column(nullable = false)
        private String parkingAddress;      // "Plot 5, Vijay Nagar, Indore"
    
        @Column(nullable = false)
        private String parkingContact;      // "9876543210"
    
        // one way fee FROM this city
        @Column(nullable = false)
        @Builder.Default
        private double oneWayFee = 500.0;   // ₹500 default

        @Column(name = "local_service_charge", precision = 10, scale = 2)
        @Builder.Default
        private BigDecimal localServiceCharge = BigDecimal.ZERO;

        @PrePersist
        public void onCreate() {
            if (this.bookingAvailable == null) {
                this.bookingAvailable = true;
            }
            if (this.localServiceCharge == null) {
                this.localServiceCharge = BigDecimal.ZERO;
            }
        }
    }