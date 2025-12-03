package event.payment.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import event.payment.model.Payment;
import event.payment.model.PaymentStatus;
import event.payment.model.PaymentType;
import event.payment.service.PaymentService;
import event.payment.web.dto.PaymentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
public class PaymentControllerAPITest {

    @MockitoBean
    private PaymentService paymentService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postUpsertPayment_shouldInvokeServiceMethodAndReturn201CreatedAndReturnPaymentResponse() throws Exception {

        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        PaymentRequest dto = PaymentRequest.builder()
                .eventId(eventId)
                .userId(userId)
                .username("testUser")
                .amount(BigDecimal.TEN)
                .type(PaymentType.SINGLE)
                .status(PaymentStatus.PAID)
                .build();

        Payment entity = Payment.builder()
                .eventId(eventId)
                .userId(userId)
                .username("testUser")
                .amount(BigDecimal.TEN)
                .type(PaymentType.SINGLE)
                .status(PaymentStatus.PAID)
                .build();

        when(paymentService.upsertPayment(any())).thenReturn(entity);

        MockHttpServletRequestBuilder httpRequest = post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(dto));

        mockMvc.perform(httpRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventId").value(eventId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.amount").value(10))
                .andExpect(jsonPath("$.type").value("SINGLE"))
                .andExpect(jsonPath("$.status").value("PAID"));

        verify(paymentService).upsertPayment(any());
    }

    @Test
    void putUpdateStatus_shouldReturn200AndUpdatedPayment() throws Exception {

        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Payment entity = Payment.builder()
                .eventId(eventId)
                .userId(userId)
                .status(PaymentStatus.PAID)
                .build();

        when(paymentService.updateStatus(eventId, userId)).thenReturn(entity);

        mockMvc.perform(put("/api/v1/payments/{eventId}/{userId}/status", eventId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(eventId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("PAID"));

        verify(paymentService).updateStatus(eventId, userId);
    }

    @Test
    void getPaymentsByEventId_shouldReturnList() throws Exception {

        UUID eventId = UUID.randomUUID();

        Payment payment = Payment.builder()
                .eventId(eventId)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentService.getAllByEventId(eventId)).thenReturn(List.of(payment));

        mockMvc.perform(get("/api/v1/payments/event")
                        .param("eventId", eventId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value(eventId.toString()))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(paymentService).getAllByEventId(eventId);
    }

}
