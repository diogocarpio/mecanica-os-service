@e2e
Feature: Saga completa entre OS Service, Billing Service e Execution Service com serviços reais

  Scenario: Fluxo feliz - OS finalizada após aprovação do orçamento, pagamento e execução
    Given que o ambiente E2E está no ar
    When eu crio uma OS com CPF "52998224792" e placa "ABC1234"
    Then a OS deve estar com status "AGUARDANDO_APROVACAO"
    And deve existir um orçamento no Billing para a OS
    When eu aprovo o orçamento no Billing
    Then a OS deve estar com status "AGUARDANDO_PAGAMENTO"
    When eu publico o evento de pagamento aprovado no RabbitMQ
    Then a OS deve estar com status "EM_EXECUCAO"
    And deve existir uma execução no Execution para a OS
    When eu completo a execução no Execution
    Then a OS deve estar com status "FINALIZADA"

  Scenario: Rejeição do orçamento cancela a OS
    Given que o ambiente E2E está no ar
    When eu crio uma OS com CPF "52998224792" e placa "ABC1234"
    Then a OS deve estar com status "AGUARDANDO_APROVACAO"
    And deve existir um orçamento no Billing para a OS
    When eu rejeito o orçamento no Billing com motivo "Cliente desistiu"
    Then a OS deve estar com status "CANCELADA"

  Scenario: Rejeição do pagamento cancela a OS
    Given que o ambiente E2E está no ar
    When eu crio uma OS com CPF "52998224792" e placa "ABC1234"
    Then a OS deve estar com status "AGUARDANDO_APROVACAO"
    And deve existir um orçamento no Billing para a OS
    When eu aprovo o orçamento no Billing
    Then a OS deve estar com status "AGUARDANDO_PAGAMENTO"
    When eu publico o evento de pagamento rejeitado no RabbitMQ
    Then a OS deve estar com status "CANCELADA"

  Scenario: Cancelamento via evento os.cancelled compensa a execução
    Given que o ambiente E2E está no ar
    When eu crio uma OS com CPF "52998224792" e placa "ABC1234"
    Then a OS deve estar com status "AGUARDANDO_APROVACAO"
    And deve existir um orçamento no Billing para a OS
    When eu aprovo o orçamento no Billing
    Then a OS deve estar com status "AGUARDANDO_PAGAMENTO"
    When eu publico o evento de pagamento aprovado no RabbitMQ
    Then a OS deve estar com status "EM_EXECUCAO"
    And deve existir uma execução no Execution para a OS
    When eu publico o evento de cancelamento da OS no RabbitMQ
    Then a OS deve estar com status "CANCELADA"
    And a execução deve estar com status "CANCELLED"
