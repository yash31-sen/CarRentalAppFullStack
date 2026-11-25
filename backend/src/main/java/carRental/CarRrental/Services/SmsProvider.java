package carRental.CarRrental.Services;

public interface SmsProvider {
    boolean isConfigured();

    /**
     * Attempts to send a verification SMS.
     *
     * @return true if SMS was dispatched by a configured provider, false if SMS delivery is unavailable/unconfigured.
     */
    boolean sendVerificationLink(String phoneNumber, String verificationUrl);
}
