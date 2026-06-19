package vallegrande.edu.pe.visons.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderResponseDTO {

    private Integer orderId;
    private Integer clientId;
    private String clientName;
    private String orderCode;
    private LocalDate orderDate;
    private String incoterm;
    private String status;
    private List<OrderDetailDTO> orderDetails = new ArrayList<>();

    public OrderResponseDTO() {
    }

    public OrderResponseDTO(Integer orderId, Integer clientId, String clientName, String orderCode, LocalDate orderDate, String incoterm, String status) {
        this(orderId, clientId, clientName, orderCode, orderDate, incoterm, status, new ArrayList<>());
    }

    public OrderResponseDTO(Integer orderId, Integer clientId, String clientName, String orderCode, LocalDate orderDate, String incoterm, String status, List<OrderDetailDTO> orderDetails) {
        this.orderId = orderId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.orderCode = orderCode;
        this.orderDate = orderDate;
        this.incoterm = incoterm;
        this.status = status;
        this.orderDetails = orderDetails == null ? new ArrayList<>() : orderDetails;
    }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public String getIncoterm() { return incoterm; }
    public void setIncoterm(String incoterm) { this.incoterm = incoterm; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<OrderDetailDTO> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetailDTO> orderDetails) { this.orderDetails = orderDetails == null ? new ArrayList<>() : orderDetails; }

    @Override
    public String toString() {
        return "OrderResponseDTO{" +
                "orderId=" + orderId +
                ", clientId=" + clientId +
                ", clientName='" + clientName + '\'' +
                ", orderCode='" + orderCode + '\'' +
                ", orderDate=" + orderDate +
                ", incoterm='" + incoterm + '\'' +
                ", status='" + status + '\'' +
                ", orderDetails=" + orderDetails +
                '}';
    }
}
