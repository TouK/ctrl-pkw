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
@EqualsAndHashCode(of = { "votesValidCount", "votesCountPerOption" })
@NoArgsConstructor
@AllArgsConstructor
public class BallotResult implements Serializable {

    private Integer includedWardsCount;

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
                .includedWardsCount(this.includedWardsCount+result.includedWardsCount)
                .votersEntitledCount(this.votersEntitledCount + result.votersEntitledCount)
                .ballotsGivenCount(this.ballotsGivenCount + result.ballotsGivenCount)
                .votesCastCount(this.votesCastCount + result.votesCastCount)
                .votesValidCount(this.votesValidCount + result.votesValidCount)
                .votesCountPerOption(
                        Lists.newArrayList(
                                summingIterator(
                                        this.votesCountPerOption.iterator(),
                                        result.votesCountPerOption.iterator(),
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
        return StreamUtils.stream(Iterables.concat(Arrays.asList(includedWardsCount, votersEntitledCount, ballotsGivenCount, votesCastCount, votesValidCount), votesCountPerOption))
                .map(i -> Optional.ofNullable(i).map(Object::toString).orElse(""))
                .collect(Collectors.joining(","));

    }

}
