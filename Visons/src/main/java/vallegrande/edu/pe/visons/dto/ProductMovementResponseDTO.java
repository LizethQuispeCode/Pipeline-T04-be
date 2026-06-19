package vallegrande.edu.pe.visons.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductMovementResponseDTO {

    private Integer orderId;
    private Integer clientId;
    private String clientName;
    private String orderCode;
    private LocalDate orderDate;
    private String incoterm;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal total;
    private List<ProductMovementDetailResponseDTO> details = new ArrayList<>();

    public ProductMovementResponseDTO() {
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<ProductMovementDetailResponseDTO> getDetails() {
        return details;
    }

    public void setDetails(List<ProductMovementDetailResponseDTO> details) {
        this.details = details == null ? new ArrayList<>() : details;
    }
}