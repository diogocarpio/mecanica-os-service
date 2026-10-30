Feature: Fluxo de ordem de serviço
  Como o microsserviço OS Service
  Eu quero validar o ciclo de vida da ordem de serviço
  Para garantir que o fluxo de abertura, aprovação e finalização funcione corretamente

  Scenario: abertura, aprovação e finalização da ordem de serviço
    Given uma OS é criada com CPF "10000000000" e placa "ABC1234"
    Then o status da OS deve ser "RECEBIDA" imediatamente após a criação
    When o billing informa que o orçamento foi gerado e a OS entrou em aguardando aprovação
    Then o status da OS deve ser "AGUARDANDO_APROVACAO"
    When o serviço de billing aprova o orçamento
    Then o status da OS deve ser "EM_EXECUCAO"
    When o serviço de execução finaliza a ordem
    Then o status da OS deve ser "FINALIZADA"
