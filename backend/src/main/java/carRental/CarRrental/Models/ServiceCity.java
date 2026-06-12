    package carRental.CarRrental.Models;
    
    import jakarta.persistence.*;
    import lombok.*;
    
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
        private boolean active = true;      // can be deactivated
    
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
        // flat fee charged when
        // car leaves this city one way
        @Column(nullable = false)
        private double oneWayFee = 500.0;   // ₹500 default
    }