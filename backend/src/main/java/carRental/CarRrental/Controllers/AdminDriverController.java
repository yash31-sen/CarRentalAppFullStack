package carRental.CarRrental.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/drivers")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminDriverController {

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void driverManagementRemoved() {
        throw new ResponseStatusException(
                HttpStatus.GONE,
                "Driver management has been removed from the Admin Portal. Use Host Management instead."
        );
    }
}
