package com.f1rsters.os_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionHandlerTest {

    @Test
    void handleValidationException_ShouldReturnBadRequestResponse() throws NoSuchMethodException {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        TestRequest request = new TestRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "request");
        bindingResult.addError(new FieldError("request", "campo", "Campo obrigatório"));

        MethodParameter parameter = new MethodParameter(TestRequest.class.getDeclaredMethod("getCampo"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        var response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertEquals("Erro de validação nos campos", ((Map<?, ?>) response.getBody()).get("message"));
        assertEquals("Campo obrigatório", ((Map<?, ?>) ((Map<?, ?>) response.getBody()).get("errors")).get("campo"));
    }

    @Test
    void handleRuntimeException_ShouldReturnBadRequestResponse() {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        var response = handler.handleRuntimeException(new RuntimeException("falha de negócio"));

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertEquals("falha de negócio", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerErrorResponse() {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        var response = handler.handleGenericException(new IllegalStateException("erro interno"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
        assertEquals("Erro interno no servidor", ((Map<?, ?>) response.getBody()).get("message"));
    }

    private static class TestRequest {
    private String campo;
    public String getCampo() { return campo; }
        public void setCampo(String campo) { this.campo = campo; }
    }
}
