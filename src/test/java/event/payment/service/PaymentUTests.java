package event.payment.service;

import event.payment.model.Payment;
import event.payment.model.PaymentStatus;
import event.payment.model.PaymentType;
import event.payment.repository.PaymentRepository;
import event.payment.web.dto.PaymentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(SpringExtension.class)
public class PaymentUTests {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void updateStatusIfPaymentDoesNotExist_thenThrowsException() {

        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(paymentRepository.findByEventIdAndUserId(eventId, userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.updateStatus(eventId, userId));
    }

    @Test
    void whenUpdateStatus_andRepositoryReturnsPending_thenPaymentIsUpdatedWithStatusPaid_andPersistedInTheDatabase() {

        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .eventId(eventId)
                .userId(UUID.randomUUID())
                .status(PaymentStatus.PENDING)
                .build();
        when(paymentRepository.findByEventIdAndUserId(eventId, userId)).thenReturn(Optional.of(payment));

        paymentService.updateStatus(eventId, userId);

        assertEquals(PaymentStatus.PAID, payment.getStatus());
        //assertThat(user.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        verify(paymentRepository).save(payment);
    }

    @Test
    void whenUpdateStatus_andRepositoryReturnsPaid_thenPaymentIsUpdatedWithStatusPending_andPersistedInTheDatabase() {

        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .userId(userId)
                .eventId(eventId)
                .userId(UUID.randomUUID())
                .status(PaymentStatus.PAID)
                .build();
        when(paymentRepository.findByEventIdAndUserId(eventId, userId)).thenReturn(Optional.of(payment));

        paymentService.updateStatus(eventId, userId);

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        //assertThat(user.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        verify(paymentRepository).save(payment);
    }

    @Test
    void getAllByUserId_whenPaymentsExist_thenReturnList() {

        UUID userId = UUID.randomUUID();
        Payment payment1 = Payment.builder().userId(userId).build();
        Payment payment2 = Payment.builder().userId(userId).build();

        when(paymentRepository.findAllByUserId(userId))
                .thenReturn(List.of(payment1, payment2));

        var result = paymentService.getAllByUserId(userId);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(payment1, payment2);

        verify(paymentRepository).findAllByUserId(userId);
    }

    @Test
    void getAllByEventId_whenPaymentsExist_thenReturnList() {

        UUID eventId = UUID.randomUUID();
        Payment payment1 = Payment.builder().eventId(eventId).build();
        Payment payment2 = Payment.builder().eventId(eventId).build();

        when(paymentRepository.findAllByEventId(eventId))
                .thenReturn(List.of(payment1, payment2));

        var result = paymentService.getAllByEventId(eventId);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(payment1, payment2);

        verify(paymentRepository).findAllByEventId(eventId);
    }

    @Test
    void upsertPayment_whenPaymentExists_thenUpdateCurrentPayment() {

        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Payment existingPayment = Payment.builder()
                .eventId(eventId)
                .userId(userId)
                .amount(BigDecimal.TEN)
                .status(PaymentStatus.PENDING)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        PaymentRequest request = PaymentRequest.builder()
                .eventId(eventId)
                .userId(userId)
                .amount(BigDecimal.ONE)
                .status(PaymentStatus.PAID)
                .type(PaymentType.SINGLE)
                .build();

        when(paymentRepository.findByEventIdAndUserId(eventId, userId))
                .thenReturn(Optional.of(existingPayment));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.upsertPayment(request);

        assertThat(result.getAmount()).isEqualTo(BigDecimal.ONE);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(result.getType()).isEqualTo(PaymentType.SINGLE);
        assertThat(result.getUpdatedOn()).isNotNull();

        verify(paymentRepository).save(existingPayment);
    }

    @Test
    void upsertPayment_whenPaymentDoesNotExist_thenCreateNewPayment() {

        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        PaymentRequest request = PaymentRequest.builder()
                .eventId(eventId)
                .userId(userId)
                .username("testUser")
                .amount(BigDecimal.TEN)
                .status(PaymentStatus.PENDING)
                .type(PaymentType.SINGLE)
                .build();

        when(paymentRepository.findByEventIdAndUserId(eventId, userId))
                .thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.upsertPayment(request);

        assertThat(result.getEventId()).isEqualTo(eventId);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("testUser");
        assertThat(result.getAmount()).isEqualTo(BigDecimal.TEN);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getType()).isEqualTo(PaymentType.SINGLE);
        assertThat(result.getCreatedOn()).isNotNull();
        assertThat(result.getUpdatedOn()).isNotNull();

        verify(paymentRepository).save(any(Payment.class));
    }
}
