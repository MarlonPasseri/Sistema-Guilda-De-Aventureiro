package br.com.guilda.registro.exception;

import br.com.guilda.registro.dto.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        ErrorResponse body = new ErrorResponse(ex.getMensagem(), ex.getDetalhes());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .filter(message -> message != null && !message.isBlank())
            .distinct()
            .toList();

        if (detalhes.isEmpty()) {
            detalhes = List.of("dados invalidos");
        }

        ErrorResponse body = new ErrorResponse("Solicitacao invalida", detalhes);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> detalhes = ex.getConstraintViolations()
            .stream()
            .map(violation -> violation.getMessage())
            .distinct()
            .toList();

        if (detalhes.isEmpty()) {
            detalhes = List.of("dados invalidos");
        }

        ErrorResponse body = new ErrorResponse("Solicitacao invalida", detalhes);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleUnreadablePayload(Exception ex) {
        String detalhe = "corpo ou parametro invalido";

        if (ex instanceof HttpMessageNotReadableException readable) {
            Throwable cause = readable.getMostSpecificCause();
            if (cause instanceof UnrecognizedPropertyException unknownProperty) {
                detalhe = "campo nao permitido: " + unknownProperty.getPropertyName();
            } else if (cause instanceof InvalidFormatException invalidFormat && !invalidFormat.getPath().isEmpty()) {
                detalhe = "valor invalido para campo: " + invalidFormat.getPath().get(0).getFieldName();
            }
        }

        ErrorResponse body = new ErrorResponse("Solicitacao invalida", List.of(detalhe));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
