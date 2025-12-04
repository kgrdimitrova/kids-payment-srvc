package event.payment.web.dto;

import event.payment.model.PaymentStatus;
import event.payment.model.PaymentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {

    private UUID eventId;

    private UUID userId;

    private String username;

    private String eventName;

    private BigDecimal amount;

    private PaymentType type;

    private PaymentStatus status;
}
