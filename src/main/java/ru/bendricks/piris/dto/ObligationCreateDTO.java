package ru.bendricks.piris.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.bendricks.piris.model.Obligation;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ObligationCreateDTO {

    private Obligation obligation;
    private int months;
    private long startBalance;

}
