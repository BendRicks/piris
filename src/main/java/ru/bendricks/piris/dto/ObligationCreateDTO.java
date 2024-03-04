package ru.bendricks.piris.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import ru.bendricks.piris.model.Obligation;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ObligationCreateDTO {

    private Obligation obligation;
    @Positive
    private int months;
    @PositiveOrZero
    private long startBalance;
    @NotBlank
    private String paymentIban;

}
