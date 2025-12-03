package event.payment.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import event.payment.model.Payment;
import event.payment.model.PaymentStatus;
import event.payment.model.PaymentType;
import event.payment.repository.PaymentRepository;
import event.payment.web.dto.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration test: boots the context with H2 and exercises controller + repository.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class PaymentITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID eventId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();

        eventId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void postUpsertPayment_shouldPersistAndReturnCreated() throws Exception {
        PaymentRequest req = PaymentRequest.builder()
                .eventId(eventId)
                .userId(userId)
                .username("integrationUser")
                .amount(BigDecimal.valueOf(15))
                .type(PaymentType.SINGLE)
                .status(PaymentStatus.PENDING)
                .build();

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventId").value(eventId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.amount").value(15))
                .andExpect(jsonPath("$.status").value("PENDING"));

        List<Payment> all = paymentRepository.findAllByEventId(eventId);
        assertThat(all).hasSize(1);
        Payment persisted = all.get(0);
        assertThat(persisted.getUserId()).isEqualTo(userId);
        assertThat(persisted.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(15));
        assertThat(persisted.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void getPaymentsByEventId_shouldReturnPersistedPayments() throws Exception {

        Payment p1 = Payment.builder()
                .eventId(eventId)
                .userId(userId)
                .username("u1")
                .amount(BigDecimal.valueOf(20))
                .status(PaymentStatus.PAID)
                .type(PaymentType.SINGLE)
                .build();
        paymentRepository.save(p1);

        mockMvc.perform(get("/api/v1/payments/event")
                        .param("eventId", eventId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value(eventId.toString()))
                .andExpect(jsonPath("$[0].status").value("PAID"));

        List<Payment> byEvent = paymentRepository.findAllByEventId(eventId);
        assertThat(byEvent).hasSize(1);
    }

    @Test
    void putUpdateStatus_shouldToggleStatusAndReturnUpdated() throws Exception {

        Payment p = Payment.builder()
                .eventId(eventId)
                .userId(userId)
                .username("toggleUser")
                .amount(BigDecimal.valueOf(5))
                .status(PaymentStatus.PENDING)
                .type(PaymentType.SINGLE)
                .build();
        paymentRepository.save(p);

        mockMvc.perform(put("/api/v1/payments/{eventId}/{userId}/status", eventId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        Payment updated = paymentRepository.findByEventIdAndUserId(eventId, userId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(PaymentStatus.PAID);

        mockMvc.perform(put("/api/v1/payments/{eventId}/{userId}/status", eventId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        Payment toggledBack = paymentRepository.findByEventIdAndUserId(eventId, userId).orElseThrow();
        assertThat(toggledBack.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }
}

