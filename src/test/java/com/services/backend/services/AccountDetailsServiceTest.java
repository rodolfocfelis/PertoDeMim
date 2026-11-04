package com.services.backend.services;
import org.junit.jupiter.api.Test;
import com.services.backend.entities.User;
import com.services.backend.entities.dtos.AccountDetails;
import static org.junit.jupiter.api.Assertions.*;
class AccountDetailsServiceTest {
    private final AccountDetailsService service=new AccountDetailsService();
    @Test void rejectsInvalidContactBeforeGeocoding() {
        var details=new AccountDetails("123","01001000","Praça da Sé","1","","Sé","São Paulo","SP","");
        assertThrows(IllegalArgumentException.class,()->service.apply(new User(),details));
    }
    @Test void keepsCoordinatesWhenOnlyContactChanges() {
        User user=new User();
        user.setStreet("Praça da Sé"); user.setAddressNumber("1"); user.setCity("São Paulo");
        user.setState("SP"); user.setZipCode("01001000"); user.setLatitude(-23.55); user.setLongitude(-46.63);
        var details=new AccountDetails("(11) 99999-9999","01001-000","Praça da Sé","1","","Sé","São Paulo","sp","");
        service.apply(user,details);
        assertEquals("11999999999",user.getPhone()); assertEquals(-23.55,user.getLatitude());
        assertEquals("SP",user.getState());
    }
}
