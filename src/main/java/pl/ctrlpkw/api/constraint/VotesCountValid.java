package pl.ctrlpkw.api.constraint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.api.dto.Protocol;
import pl.ctrlpkw.model.read.BallotOptionRepository;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VotesCountValid.Validator.class)
public @interface VotesCountValid {

    String message() default "{pl.ctrlpkw.api.constraint.VotesCountValid.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Slf4j
    public class Validator implements ConstraintValidator<VotesCountValid, Protocol>{
        @Autowired
        private BallotOptionRepository ballotOptionRepository;

        @Override
        public void initialize(VotesCountValid constraintAnnotation) {}

        @Override
        public boolean isValid(Protocol protocol, ConstraintValidatorContext context) {
            long optionsCount = ballotOptionRepository.countByDateAndNo(protocol.getVotingDate(), protocol.getBallotNo());
            BallotResult ballotResult = protocol.getBallotResult();
            boolean result = /*ballotResult.getVotersEntitledCount() >= ballotResult.getBallotsGivenCount()
                    && ballotResult.getBallotsGivenCount() >= ballotResult.getVotesCastCount()
                    && ballotResult.getVotesCastCount() >= ballotResult.getVotesValidCount()
                    &&*/ ballotResult.getVotesCountPerOption().size() == optionsCount
                    && ballotResult.getVotesValidCount() == ballotResult.getVotesCountPerOption().stream().mapToLong(Long::longValue).sum()
                    ;
            return result;
        }
    }
}
