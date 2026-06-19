package vallegrande.edu.pe.visons.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ProviderSummaryDTO {
    private Integer providerId;
    private String companyName;
    private String taxId;
    private String productType;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long totalPurchases;
    private Double totalVolumeKg;
    private Double totalAmount;
    private LocalDateTime lastPurchaseDate;
    private List<String> mainProducts;

    public Integer getProviderId() { return providerId; }
    public void setProviderId(Integer providerId) { this.providerId = providerId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(Long totalPurchases) { this.totalPurchases = totalPurchases; }
    public Double getTotalVolumeKg() { return totalVolumeKg; }
    public void setTotalVolumeKg(Double totalVolumeKg) { this.totalVolumeKg = totalVolumeKg; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public LocalDateTime getLastPurchaseDate() { return lastPurchaseDate; }
    public void setLastPurchaseDate(LocalDateTime lastPurchaseDate) { this.lastPurchaseDate = lastPurchaseDate; }
    public List<String> getMainProducts() { return mainProducts; }
    public void setMainProducts(List<String> mainProducts) { this.mainProducts = mainProducts; }
}
