package pl.codeinprogress.notes.presenter;

import org.junit.Test;

import pl.codeinprogress.notes.util.Encryption;

import static org.junit.Assert.*;

/**
 * Created by tomaszmartin on 09.07.2016.
 */

public class EncryptionTest {

    @Test
    public void savesPassword() throws Exception {
        String password = "password";
        Encryption encryption = new Encryption(password);
        assertEquals(password, encryption.getPassword());
    }

    @Test
    public void encryptsMessage() throws Exception {
        Encryption encryption = new Encryption("password");
        String message = "message";

        String encrypted = encryption.encrypt(message);
        String dectypted = encryption.decrypt(encrypted);

        assertEquals(dectypted, message);
    }

}