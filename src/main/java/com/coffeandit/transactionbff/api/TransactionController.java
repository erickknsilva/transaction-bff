package com.coffeandit.transactionbff.api;

import com.coffeandit.transactionbff.domain.TransactionService;
import com.coffeandit.transactionbff.dto.LimiteDiario;
import com.coffeandit.transactionbff.dto.RequestTransactionDto;
import com.coffeandit.transactionbff.dto.TransactionDto;
import com.coffeandit.transactionbff.exceptions.TransactionNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/transaction")
@Tag(name = "/transaction", description = "Grupo de API's para manipulação de transações financeiras")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(description = "API para criar uma transação financeira")
    @ResponseBody
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Retorno OK com a transação criada."),
            @ApiResponse(responseCode = "401", description = "Erro de autenticação dessa API"),
            @ApiResponse(responseCode = "403", description = "Erro de autorização dessa API"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<RequestTransactionDto> enviarTransacao(@RequestBody final RequestTransactionDto requestTransactionDto) {

        return transactionService.save(requestTransactionDto);

    }

    @Operation(description = "API para buscar os transações por id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Retorno OK da Lista de transações"),
            @ApiResponse(responseCode = "401", description = "Erro de autenticação dessa API"),
            @ApiResponse(responseCode = "403", description = "Erro de autorização dessa API"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")})
    @Parameters(value = {@Parameter(name = "id", in = ParameterIn.PATH)})
    @ResponseBody
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TransactionDto> findById(@PathVariable("id") final String id) {

        final Optional<TransactionDto> dto = transactionService.findById(id);

        if (dto.isPresent()) {
            return Mono.just(dto.get());
        }
        throw new TransactionNotFoundException("Unable to find resource");
    }

    @Operation(description = "API para remover as transações persistidas")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Retorno OK da remoçao"),
            @ApiResponse(responseCode = "401", description = "Erro de autenticação dessa API"),
            @ApiResponse(responseCode = "403", description = "Erro de autorização dessa API"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")})
    @Parameters(value = {@Parameter(name = "id", in = ParameterIn.PATH)})
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TransactionDto> removerTransacao(@PathVariable("id") final String uuid) {
        return Mono.empty();
    }

    @Operation(description = "API para autorizar a transaçao financeira")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Retorno OK da remoçao"),
            @ApiResponse(responseCode = "401", description = "Erro de autenticação dessa API"),
            @ApiResponse(responseCode = "403", description = "Erro de autorização dessa API"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")})
    @Parameters(value = {@Parameter(name = "id", in = ParameterIn.PATH)})
    @PatchMapping(value = "/{id}/confimar")
    public Mono<TransactionDto> confirmarTransacao(@PathVariable("id") final String uuid) {
        return Mono.empty();
    }


    @GetMapping("/{agencia}/{conta}")
    public Flux<List<TransactionDto>> buscarTransacoes(@PathVariable("agencia") final Long agencia,
                                                       @PathVariable("conta") final Long conta) {

        return transactionService.findAllAgenciaAndConta(agencia, conta);

    }

    @GetMapping("/sse/{agencia}/{conta}")
    public Flux<ServerSentEvent<List<TransactionDto>>> buscarTransacoesSee(@PathVariable("agencia") final Long agencia,
                                                                           @PathVariable("conta") final Long conta) {

        return Flux.interval(Duration.ofSeconds(5))
                .map(sequence -> ServerSentEvent.<List<TransactionDto>>builder()
                        .id(String.valueOf(sequence))
                        .event("transacoes")
                        .data(transactionService.findByAgenciaAndConta(agencia, conta))
                        .retry(Duration.ofSeconds(3))
                        .build()
                );
    }

}
