package ru.bendricks.piris.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.bendricks.piris.dto.ObligationCreateDTO;
import ru.bendricks.piris.model.Obligation;
import ru.bendricks.piris.service.ObligationService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/obligation")
public class ObligationController {

    private final ObligationService obligationService;

    @PostMapping("/deposit/create")
    @ResponseBody
    public Obligation createDeposit(@RequestBody @Valid ObligationCreateDTO obligationCreateDTO) throws Exception {
        return obligationService.createDepositObligation(obligationCreateDTO.getObligation(), obligationCreateDTO.getStartBalance());
    }

}
