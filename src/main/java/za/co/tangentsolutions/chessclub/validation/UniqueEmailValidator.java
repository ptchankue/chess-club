package za.co.tangentsolutions.chessclub.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;
import za.co.tangentsolutions.chessclub.repositories.MemberRepository;

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String>, BeanFactoryAware {

    private BeanFactory beanFactory;
    private MemberRepository memberRepository;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
        // Get the repository from the bean factory when the validator is initialized
        try {
            this.memberRepository = beanFactory.getBean(MemberRepository.class);
        } catch (Exception e) {
            // If we can't get the repository, we'll handle it gracefully
            System.err.println("Warning: Could not get MemberRepository bean: " + e.getMessage());
        }
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        // If email is null or blank, delegate to @Email or @NotBlank annotations.
        if (email == null || email.isBlank()) {
            return true;
        }

        // Use your repository to check if an email already exists
        if (memberRepository != null) {
            try {
                return !memberRepository.existsByEmail(email);
            } catch (Exception e) {
                // If there's an error checking the email, assume it's valid to avoid blocking
                System.err.println("Error checking email uniqueness: " + e.getMessage());
                return true;
            }
        }
        
        // Fallback if repository is not available
        return true;
    }
}
