package event.payment.repository;

import event.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findAllByEventId(UUID eventId);

    List<Payment> findAllByUserId(UUID userId);

    Optional<Payment> findByEventIdAndUserId(UUID eventId, UUID userId);
}
