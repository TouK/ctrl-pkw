package pl.ctrlpkw.api.dto;

import com.codepoetics.protonpack.StreamUtils;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

@ApiModel
@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BallotResult implements Serializable {

    @NotNull
    private Long votersEntitledCount;

    @NotNull
    private Long ballotsGivenCount;

    @NotNull
    private Long votesCastCount;

    @NotNull
    private Long votesValidCount;

    private List<Long> votesCountPerOption;

    public BallotResult add(BallotResult result) {
        return BallotResult.builder()
                .votersEntitledCount(this.getVotersEntitledCount() + result.getVotersEntitledCount())
                .ballotsGivenCount(this.getBallotsGivenCount() + result.getBallotsGivenCount())
                .votesCastCount(this.getVotesCastCount() + result.getVotesCastCount())
                .votesValidCount(this.getVotesValidCount() + result.getVotesValidCount())
                .votesCountPerOption(
                        Lists.newArrayList(
                                summingIterator(
                                        this.getVotesCountPerOption().iterator(),
                                        result.getVotesCountPerOption().iterator(),
                                        0l,
                                        (a, b) -> a + b
                                )
                        )
                )
                .build();

    }

    private <T> Iterator<T> summingIterator(Iterator<T> i1, Iterator<T> i2, T zero, BinaryOperator<T> addition) {

        return new AbstractIterator<T>() {
            @Override
            protected T computeNext() {
                if (!i1.hasNext() && !i2.hasNext())
                    return endOfData();
                T sum = i1.hasNext() ? i1.next() : zero;
                if (i2.hasNext()) sum = addition.apply(sum, i2.next());
                return sum;
            }
        };
    }

    public String toString() {
        return StreamUtils.stream(Iterables.concat(Arrays.asList(votersEntitledCount, ballotsGivenCount, votesCastCount, votesValidCount), votesCountPerOption))
                .map(i -> Optional.ofNullable(i).map(Object::toString).orElse(""))
                .collect(Collectors.joining(","));

    }

}
