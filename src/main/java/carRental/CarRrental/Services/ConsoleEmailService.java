package carRental.CarRrental.Services;

import org.springframework.stereotype.Service;

@Service
public class ConsoleEmailService implements EmailService {

    @Override
    public void sendEmail(String to, String subject, String body) {
        System.out.println("====== EMAIL (DEV MODE) ======");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body:\n" + body);
        System.out.println("==============================");
    }
}
