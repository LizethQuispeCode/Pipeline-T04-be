package vallegrande.edu.pe.visons.service;

public interface OrderPdfService {
    byte[] generateOrderPdf(Integer orderId) throws Exception;
    byte[] generateOrdersReportPdf() throws Exception;
}
