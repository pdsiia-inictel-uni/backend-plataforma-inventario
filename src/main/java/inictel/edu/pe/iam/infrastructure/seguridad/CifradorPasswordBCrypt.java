package inictel.edu.pe.iam.infrastructure.seguridad;

import inictel.edu.pe.iam.domain.service.CifradorPassword;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptador de cifrado con BCrypt, factor de costo 10 (RF-05, RNF-03).
 */
@Component
public class CifradorPasswordBCrypt implements CifradorPassword {

    private final PasswordEncoder encoder;

    public CifradorPasswordBCrypt(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String cifrar(String passwordPlana) {
        return encoder.encode(passwordPlana);
    }

    @Override
    public boolean coincide(String passwordPlana, String hash) {
        return encoder.matches(passwordPlana, hash);
    }
}
