package com.f1rsters.os_service.dto;

import com.f1rsters.os_service.validation.CpfCnpjValido;
import com.f1rsters.os_service.validation.PlacaValida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CriarOrdemServicoDTO {
    @NotBlank(message = "CPF/CNPJ do cliente é obrigatório")
    @CpfCnpjValido
    public String cpfCnpjCliente;

    @NotBlank(message = "Placa do veículo é obrigatória")
    @PlacaValida
    public String placaVeiculo;

    @NotEmpty(message = "Pelo menos um serviço deve ser informado")
    public List<String> servicosIds; // IDs dos serviços solicitados

    public List<String> pecasIds; // IDs das peças usadas (opcional)
}
