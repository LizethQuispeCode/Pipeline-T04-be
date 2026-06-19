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
public class PurchaseRequestDTO {

    @NotNull(message = "providerId es requerido")
    private Integer providerId;

    @NotBlank(message = "purchaseCode es requerido")
    @Size(min = 3, max = 50, message = "purchaseCode debe tener entre 3 y 50 caracteres")
    private String purchaseCode;

    private LocalDate purchaseDate;

    @Size(max = 500, message = "notes debe tener como máximo 500 caracteres")
    private String notes;

    @Valid
    @NotNull(message = "details es requerido")
    private List<@Valid PurchaseDetailRequestDTO> details = new ArrayList<>();

    public PurchaseRequestDTO() {
    }

    public Integer getProviderId() {
        return providerId;
    }

    public void setProviderId(Integer providerId) {
        this.providerId = providerId;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<PurchaseDetailRequestDTO> getDetails() {
        return details;
    }

    public void setDetails(List<PurchaseDetailRequestDTO> details) {
        this.details = details == null ? new ArrayList<>() : details;
    }
}
