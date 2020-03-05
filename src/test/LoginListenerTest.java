package test;

import listeners.LoginListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LoginListenerTest {

    @Test
    void errorName() {
        String actual = LoginListener.errorName("Tommaso", "localhost");
        Assertions.assertEquals("PASSED", actual);
    }
}