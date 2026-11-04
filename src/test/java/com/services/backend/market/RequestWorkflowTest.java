package com.services.backend.market;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class RequestWorkflowTest {

  private final RequestWorkflow workflow = new RequestWorkflow();

  @Test
  void professionalCanAcceptAndInformCompletion() {
    assertEquals(
      RequestStatus.ACCEPTED,
      workflow.next(
        RequestStatus.REQUESTED,
        RequestStatus.ACCEPTED,
        false,
        true
      )
    );
    assertEquals(
      RequestStatus.AWAITING_CONFIRMATION,
      workflow.next(
        RequestStatus.ACCEPTED,
        RequestStatus.AWAITING_CONFIRMATION,
        false,
        true
      )
    );
  }

  @Test
  void onlyCustomerConfirmsCompletion() {
    assertEquals(
      RequestStatus.COMPLETED,
      workflow.next(
        RequestStatus.AWAITING_CONFIRMATION,
        RequestStatus.COMPLETED,
        true,
        false
      )
    );
    assertThrows(ResponseStatusException.class, () ->
      workflow.next(
        RequestStatus.AWAITING_CONFIRMATION,
        RequestStatus.COMPLETED,
        false,
        true
      )
    );
  }

  @Test
  void terminalStatesCannotBeReopened() {
    for (var state : new RequestStatus[] {
      RequestStatus.COMPLETED,
      RequestStatus.CANCELLED,
      RequestStatus.DECLINED,
    })
      assertThrows(ResponseStatusException.class, () ->
        workflow.next(state, RequestStatus.REQUESTED, true, true)
      );
  }

  @Test
  void customerCannotAcceptTheirOwnRequest() {
    assertThrows(ResponseStatusException.class, () ->
      workflow.next(
        RequestStatus.REQUESTED,
        RequestStatus.ACCEPTED,
        true,
        false
      )
    );
  }

  @Test
  void customerCanCancelOnlyPendingOrAcceptedRequests() {
    assertEquals(
      RequestStatus.CANCELLED,
      workflow.next(
        RequestStatus.REQUESTED,
        RequestStatus.CANCELLED,
        true,
        false
      )
    );
    assertEquals(
      RequestStatus.CANCELLED,
      workflow.next(
        RequestStatus.ACCEPTED,
        RequestStatus.CANCELLED,
        true,
        false
      )
    );
    assertThrows(ResponseStatusException.class, () ->
      workflow.next(
        RequestStatus.AWAITING_CONFIRMATION,
        RequestStatus.CANCELLED,
        true,
        false
      )
    );
  }
}
