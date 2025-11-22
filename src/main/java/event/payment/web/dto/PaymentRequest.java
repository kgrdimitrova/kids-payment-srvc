package event.payment.web.dto;

import event.payment.model.PaymentStatus;
import event.payment.model.PaymentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PaymentRequest {

    private UUID userId;

    private UUID eventId;

    private String username;

    private BigDecimal amount;

    private PaymentType type;

    private PaymentStatus status;
}
