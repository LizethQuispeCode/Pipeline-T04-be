package vallegrande.edu.pe.visons.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "UBIGEO")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Ubigeo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ubigeo_id")
    private Integer ubigeoId;

    @Column(name = "ubigeo_code", nullable = false, unique = true, length = 6)
    private String ubigeoCode;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "province", nullable = false, length = 100)
    private String province;

    @Column(name = "district", nullable = false, length = 100)
    private String district;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @PrePersist
    public void prePersist() {
        if (isActive == null) {
            isActive = Boolean.TRUE;
        }
    }
}
