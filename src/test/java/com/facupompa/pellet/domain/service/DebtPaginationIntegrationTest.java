package com.facupompa.pellet.domain.service;

import com.facupompa.pellet.domain.dto.DebtPageResponse;
import com.facupompa.pellet.domain.dto.DebtResponse;
import com.facupompa.pellet.domain.exception.CustomerNotFoundException;
import com.facupompa.pellet.domain.exception.InvalidPaginationException;
import com.facupompa.pellet.persistence.entity.CustomerEntity;
import com.facupompa.pellet.persistence.entity.DebtEntity;
import com.facupompa.pellet.persistence.repository.CustomerRepository;
import com.facupompa.pellet.persistence.repository.DebtRepository;
import com.facupompa.pellet.web.controller.DebtController;
import com.facupompa.pellet.web.exception.RestExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class DebtPaginationIntegrationTest {
    @Autowired private CustomerRepository customers;
    @Autowired private DebtRepository debts;
    @Autowired private DebtService service;
    private CustomerEntity customer;
    private DebtEntity first;
    private DebtEntity second;
    private DebtEntity third;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        customer = customers.save(new CustomerEntity("Cliente paginacion"));
        third = debt(customer, "Vence despues", LocalDate.of(2027, 2, 1));
        first = debt(customer, "Primera por id", LocalDate.of(2027, 1, 1));
        second = debt(customer, "Segunda por id", LocalDate.of(2027, 1, 1));
        CustomerEntity other = customers.save(new CustomerEntity("Otro cliente"));
        debt(other, "No pertenece al cliente", LocalDate.of(2026, 1, 1));
        debts.flush();
        // Comprueba el contrato MVC con servicio/base reales; no la cadena de seguridad.
        mvc = MockMvcBuilders.standaloneSetup(new DebtController(service))
                .setControllerAdvice(new RestExceptionHandler()).build();
    }

    private DebtEntity debt(CustomerEntity owner, String description, LocalDate dueDate) {
        return debts.save(new DebtEntity(owner, description, new BigDecimal("100.25"), dueDate));
    }

    @Test
    void pagesShouldFilterCustomerAndOrderByDueDateThenId() {
        DebtPageResponse page = service.findDebtsByCustomerId(customer.getId(), 0, 2);
        assertEquals(List.of(first.getId(), second.getId()),
                page.content().stream().map(DebtResponse::id).toList());
        assertEquals(0, page.page());
        assertEquals(2, page.size());
        assertEquals(3, page.totalElements());
        assertEquals(2, page.totalPages());
        assertFalse(page.last());
        assertTrue(page.content().stream().allMatch(d -> d.customerId().equals(customer.getId())));
        assertEquals(0, new BigDecimal("100.25").compareTo(page.content().getFirst().originalAmount()));

        DebtPageResponse last = service.findDebtsByCustomerId(customer.getId(), 1, 2);
        assertEquals(List.of(third.getId()), last.content().stream().map(DebtResponse::id).toList());
        assertTrue(last.last());
    }

    @Test
    void outOfRangePageShouldBeEmptyAndPreserveTotals() {
        DebtPageResponse page = service.findDebtsByCustomerId(customer.getId(), 999, 2);
        assertTrue(page.content().isEmpty());
        assertEquals(999, page.page());
        assertEquals(3, page.totalElements());
        assertEquals(2, page.totalPages());
        assertTrue(page.last());
    }

    @Test
    void existingCustomerWithoutDebtsShouldHaveEmptyPage() {
        CustomerEntity empty = customers.save(new CustomerEntity("Sin deudas"));
        DebtPageResponse page = service.findDebtsByCustomerId(empty.getId(), 0, 20);
        assertTrue(page.content().isEmpty());
        assertEquals(0, page.totalElements());
        assertEquals(0, page.totalPages());
        assertTrue(page.last());
    }

    @Test
    void missingCustomerShouldFail() {
        assertThrows(CustomerNotFoundException.class, () -> service.findDebtsByCustomerId(-1L, 0, 20));
    }

    @Test
    void invalidParametersShouldBeRejectedAndBoundariesAccepted() {
        for (int[] values : new int[][]{{-1, 20}, {0, 0}, {0, -1}, {0, 101}}) {
            assertThrows(InvalidPaginationException.class,
                    () -> service.findDebtsByCustomerId(customer.getId(), values[0], values[1]));
        }
        assertEquals(1, service.findDebtsByCustomerId(customer.getId(), 0, 1).content().size());
        assertEquals(3, service.findDebtsByCustomerId(customer.getId(), 0, 100).content().size());
    }

    @Test
    void httpShouldReturnDefaultAndExplicitPageMetadata() throws Exception {
        String url = "/api/debts/customer/" + customer.getId();
        mvc.perform(get(url)).andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content.length()").value(3));
        mvc.perform(get(url).param("page", "1").param("size", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.content[0].id").value(third.getId()))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void httpShouldReturn400ForInvalidOrNonNumericParametersAnd404ForMissingCustomer() throws Exception {
        String url = "/api/debts/customer/" + customer.getId();
        mvc.perform(get(url).param("page", "-1")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid-pagination"));
        mvc.perform(get(url).param("size", "101")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid-pagination"));
        mvc.perform(get(url).param("page", "hola")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/debts/customer/-1")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("customer-not-found"));
    }
}
