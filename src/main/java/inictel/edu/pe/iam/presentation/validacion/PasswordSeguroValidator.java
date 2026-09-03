package inictel.edu.pe.iam.presentation.validacion;

import inictel.edu.pe.iam.domain.service.PoliticaPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Adapta la politica de contrasenas del dominio a Bean Validation, de modo que
 * la regla se define una sola vez (RNF-05).
 */
public class PasswordSeguroValidator implements ConstraintValidator<PasswordSeguro, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        return PoliticaPassword.esValida(valor);
    }
}
