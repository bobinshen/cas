package org.apereo.cas.shell.commands.jasypt;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JasyptDecryptPropertyCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SHELL")
class JasyptDecryptPropertyCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() {
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "decrypt-value --value {cas-cipher}iARpnWTURDdiAhWdcHXxqJpncj4iRo3w9i2UT33stcs= "
            + "--password JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC"));
    }

    @Test
    void verifyOperationWithInitVector() {
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "decrypt-value --value {cas-cipher}vGJIJnpRIMoB7cusy2f5ogn9gJI/8n8kBr6D/ce62QjnBLdfe5dcPcNWKL7ypaKp "
                + "--password JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC"));
    }
}


