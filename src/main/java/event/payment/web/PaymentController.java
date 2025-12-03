package event.payment.web;

import event.payment.model.Payment;
import event.payment.service.PaymentService;
import event.payment.web.dto.PaymentRequest;
import event.payment.web.dto.PaymentResponse;
import event.payment.web.mapper.DtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> upsertPayment(@RequestBody PaymentRequest request) {

        Payment payment = paymentService.upsertPayment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DtoMapper.from(payment));
    }

    @PutMapping("/{eventId}/{userId}/status")
    public ResponseEntity<PaymentResponse> updateStatus(@PathVariable UUID eventId, @PathVariable UUID userId) {

        Payment payment = paymentService.updateStatus(eventId, userId);

        return ResponseEntity
                .ok(DtoMapper.from(payment));
    }

    @GetMapping("/event")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByEventId(@RequestParam("eventId") UUID eventId) {

        List<Payment> payments = paymentService.getAllByEventId(eventId);
        List<PaymentResponse> responses = payments.stream().map(DtoMapper::from).toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@RequestParam("userId") UUID userId) {

        List<Payment> payments = paymentService.getAllByUserId(userId);
        List<PaymentResponse> responses = payments.stream().map(DtoMapper::from).toList();

        return ResponseEntity.ok(responses);
    }
}
