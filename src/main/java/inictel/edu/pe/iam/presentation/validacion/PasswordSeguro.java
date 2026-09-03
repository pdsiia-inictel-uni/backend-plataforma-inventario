package inictel.edu.pe.iam.presentation.validacion;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Politica de contrasenas (RNF-05): minimo 8 caracteres, con letras, numeros
 * y al menos un caracter especial.
 */
@Documented
@Constraint(validatedBy = PasswordSeguroValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordSeguro {

    String message() default "La contrasena debe tener al menos 8 caracteres e incluir letras, numeros y al menos un caracter especial.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
