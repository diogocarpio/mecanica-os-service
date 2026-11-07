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

  Scenario: rejeição do orçamento pelo billing service cancela a ordem de serviço
    Given uma OS é criada com CPF "20000000000" e placa "XYZ9876"
    When o billing informa que o orçamento foi gerado e a OS entrou em aguardando aprovação
    And o billing service publica o evento de orçamento rejeitado
    Then o status da OS deve ser "CANCELADA"

  Scenario: aprovação e execução concluída via eventos do saga
    Given uma OS é criada com CPF "30000000000" e placa "QWE4321"
    When o billing informa que o orçamento foi gerado e a OS entrou em aguardando aprovação
    And o billing service publica o evento de orçamento aprovado
    Then o status da OS deve ser "EM_EXECUCAO"
    When o execution service publica o evento de execução concluída
    Then o status da OS deve ser "FINALIZADA"

  Scenario: cancelamento da ordem de serviço via evento
    Given uma OS é criada com CPF "40000000000" e placa "RTY8765"
    When um evento de cancelamento da OS é recebido
    Then o status da OS deve ser "CANCELADA"
