package za.co.tangentsolutions.chessclub.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import za.co.tangentsolutions.chessclub.repositories.MemberRepository;

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    @Autowired
    private MemberRepository memberRepository; // Inject your repository

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        // If email is null or blank, delegate to @Email or @NotBlank annotations.
        if (email == null || email.isBlank()) {
            return true;
        }

        // Use your repository to check if an email already exists
        return !memberRepository.existsByEmail(email);
    }
}
