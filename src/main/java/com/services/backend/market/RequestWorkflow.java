package com.services.backend.market;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class RequestWorkflow {

  public RequestStatus next(
    RequestStatus current,
    RequestStatus target,
    boolean customer,
    boolean professional
  ) {
    boolean allowed =
      (customer &&
        target == RequestStatus.CANCELLED &&
        (current == RequestStatus.REQUESTED ||
          current == RequestStatus.ACCEPTED)) ||
      (customer &&
        target == RequestStatus.COMPLETED &&
        current == RequestStatus.AWAITING_CONFIRMATION) ||
      (professional &&
        current == RequestStatus.REQUESTED &&
        (target == RequestStatus.ACCEPTED ||
          target == RequestStatus.DECLINED)) ||
      (professional &&
        current == RequestStatus.ACCEPTED &&
        target == RequestStatus.AWAITING_CONFIRMATION);
    if (!allowed) throw new ResponseStatusException(
      HttpStatus.CONFLICT,
      "Mudança de status não permitida."
    );
    return target;
  }
}
