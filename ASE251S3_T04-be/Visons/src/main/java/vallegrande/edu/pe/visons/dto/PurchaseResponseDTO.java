package vallegrande.edu.pe.visons.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class PurchaseResponseDTO {

    private Integer purchaseId;
    private Integer providerId;
    private String providerName;
    private String providerTaxId;
    private String purchaseCode;
    private LocalDate purchaseDate;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime createdAt;
    private List<PurchaseDetailResponseDTO> details = new ArrayList<>();

    public PurchaseResponseDTO() {
    }

    public Integer getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(Integer purchaseId) {
        this.purchaseId = purchaseId;
    }

    public Integer getProviderId() {
        return providerId;
    }

    public void setProviderId(Integer providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderTaxId() {
        return providerTaxId;
    }

    public void setProviderTaxId(String providerTaxId) {
        this.providerTaxId = providerTaxId;
    }

    public String getPurchaseCode() {
        return purchaseCode;
    }

    public void setPurchaseCode(String purchaseCode) {
        this.purchaseCode = purchaseCode;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<PurchaseDetailResponseDTO> getDetails() {
        return details;
    }

    public void setDetails(List<PurchaseDetailResponseDTO> details) {
        this.details = details == null ? new ArrayList<>() : details;
    }
}
