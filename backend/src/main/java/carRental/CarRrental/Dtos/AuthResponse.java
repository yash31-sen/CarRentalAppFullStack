package carRental.CarRrental.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType; // "Bearer"
    private String refreshToken;

    private Long userId;
    private String name;
    private String email;
    private boolean emailVerified;
    private boolean mobileVerified;
    private boolean active;
    private boolean suspended;
    private List<String> roles;
    private String hostApplicationStatus;

    public AuthResponse(String token, String tokenType, String refreshToken) {
        this.token = token;
        this.tokenType = tokenType;
        this.refreshToken = refreshToken;
    }
}
