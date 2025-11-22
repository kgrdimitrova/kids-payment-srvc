package event.payment.service;

import event.payment.model.Payment;
import event.payment.model.PaymentStatus;
import event.payment.repository.PaymentRepository;
import event.payment.web.dto.PaymentRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> getAllByUserId(UUID userId) {
        return paymentRepository.findAllByUserId(userId);
    }

    public List<Payment> getAllByEventId(UUID eventId) {
        return paymentRepository.findAllByEventId(eventId);
    }

    public Payment upsertPayment(PaymentRequest paymentRequest) {

        Optional<Payment> existingPaymentOpt =
                paymentRepository.findByEventIdAndUserId(
                        paymentRequest.getEventId(),
                        paymentRequest.getUserId()
                );

        Payment payment;

        if (existingPaymentOpt.isPresent()) {
            // Update existing payment
            payment = existingPaymentOpt.get();
            payment.setAmount(paymentRequest.getAmount());
            payment.setType(paymentRequest.getType());
            payment.setStatus(paymentRequest.getStatus());
            payment.setUpdatedOn(LocalDateTime.now());
        } else {
            // Create new payment
            payment = Payment.builder()
                    .userId(paymentRequest.getUserId())
                    .username(paymentRequest.getUsername())
                    .eventId(paymentRequest.getEventId())
                    .amount(paymentRequest.getAmount())
                    .status(paymentRequest.getStatus())
                    .type(paymentRequest.getType())
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .build();
        }

        return paymentRepository.save(payment);
        }

        public Payment updateStatus(UUID eventID, UUID userId) {

        Payment payment = getPayment(eventID, userId);

            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.PAID);
                throw new RuntimeException("Payment is already pending");
            } else {
                payment.setStatus(PaymentStatus.PENDING);
                throw new RuntimeException("Payment is already pending");
            }

        //return paymentRepository.save(payment);
        }

        private Payment getPayment(UUID eventId, UUID userId) {
            Optional<Payment> existingPaymentOpt =
                    paymentRepository.findByEventIdAndUserId(
                            eventId,
                            userId
                    );
            return existingPaymentOpt.get();
        }
    }

