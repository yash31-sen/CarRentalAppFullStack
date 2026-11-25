package carRental.CarRrental.Config;

import carRental.CarRrental.Models.AppUser;
import carRental.CarRrental.Repositories.AppUserRepository;
import carRental.CarRrental.Services.CustomUserDetailsService;
import carRental.CarRrental.Services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AppUserRepository userRepository;

    public JwtAuthFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            AppUserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getServletPath();
        if (path.startsWith("/auth")
                && !path.equals("/auth/logout")
                && !path.equals("/auth/mobile/send-verification")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractEmail(token);
        Integer tokenVersionClaim = jwtService.extractTokenVersion(token);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            AppUser user = userRepository.findByEmail(email).orElse(null);
            if (user == null || !user.isActive() || user.isSuspendedFlag() || !user.isEmailVerified()) {
                filterChain.doFilter(request, response);
                return;
            }
            int currentVersion = user.getTokenVersion() == null ? 0 : user.getTokenVersion();
            if (tokenVersionClaim != null && !Objects.equals(tokenVersionClaim, currentVersion)) {
                filterChain.doFilter(request, response);
                return;
            }

            var userDetails = userDetailsService.loadUserByUsername(email);
            var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
