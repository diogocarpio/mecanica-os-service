package com.f1rsters.os_service.domain;

public enum StatusOrdemServico {
    RECEBIDA,
    DIAGNOSTICO,
    AGUARDANDO_APROVACAO,
    AGUARDANDO_PAGAMENTO,
    EM_EXECUCAO,
    FINALIZADA,
    ENTREGUE,
    CANCELADA
}
