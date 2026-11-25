package carRental.CarRrental.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UnconfiguredSmsProvider implements SmsProvider {

    private static final Logger log = LoggerFactory.getLogger(UnconfiguredSmsProvider.class);

    @Override
    public boolean isConfigured() {
        return false;
    }

    @Override
    public boolean sendVerificationLink(String phoneNumber, String verificationUrl) {
        // Never fake mobile verification or log raw verification URLs/tokens.
        log.info("SMS provider not configured; mobile verification remains pending.");
        return false;
    }
}
