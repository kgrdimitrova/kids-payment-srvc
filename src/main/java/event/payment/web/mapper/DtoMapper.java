package event.payment.web.mapper;

import event.payment.model.Payment;
import event.payment.web.dto.PaymentResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static PaymentResponse from(Payment payment) {

        return PaymentResponse.builder()
                .eventId(payment.getEventId())
                .userId(payment.getUserId())
                .username(payment.getUsername())
                .eventName(payment.getEventName())
                .amount(payment.getAmount())
                .type(payment.getType())
                .status(payment.getStatus())
                .build();
    }
}
