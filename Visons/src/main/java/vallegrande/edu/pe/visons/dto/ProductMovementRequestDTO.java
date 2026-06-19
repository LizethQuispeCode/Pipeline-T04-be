package vallegrande.edu.pe.visons.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class ProductMovementRequestDTO {

    @NotNull(message = "clientId es requerido")
    private Integer clientId;

    @NotBlank(message = "orderCode es requerido")
    @Size(min = 3, max = 50, message = "orderCode debe tener entre 3 y 50 caracteres")
    private String orderCode;

    private LocalDate orderDate;

    @Size(max = 3, message = "incoterm debe tener como máximo 3 caracteres")
    private String incoterm;

    @Valid
    @NotNull(message = "details es requerido")
    private List<@Valid ProductMovementDetailRequestDTO> details = new ArrayList<>();

    public ProductMovementRequestDTO() {
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getIncoterm() {
        return incoterm;
    }

    public void setIncoterm(String incoterm) {
        this.incoterm = incoterm;
    }

    public List<ProductMovementDetailRequestDTO> getDetails() {
        return details;
    }

    public void setDetails(List<ProductMovementDetailRequestDTO> details) {
        this.details = details == null ? new ArrayList<>() : details;
    }
}