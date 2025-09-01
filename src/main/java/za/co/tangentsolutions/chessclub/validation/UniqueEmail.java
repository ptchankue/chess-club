package za.co.tangentsolutions.chessclub.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
@Documented
public @interface UniqueEmail {
    String message() default "Email is already registered";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
