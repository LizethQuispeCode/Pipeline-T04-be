package vallegrande.edu.pe.visons.service.impl;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import vallegrande.edu.pe.visons.model.Order;
import vallegrande.edu.pe.visons.model.Customer;
import vallegrande.edu.pe.visons.repository.OrderRepository;
import vallegrande.edu.pe.visons.service.OrderPdfService;

@Service
public class OrderPdfServiceImpl implements OrderPdfService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public byte[] generateOrderPdf(Integer orderId) throws Exception {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        Customer c = order.getCustomer();

        String sql = "SELECT od.order_detail_id, p.name AS product_name, od.quantity_kg, od.unit_price "
                + "FROM ORDER_DETAILS od JOIN PRODUCTS p ON od.product_id = p.product_id WHERE od.order_id = ?";

        List<Map<String, Object>> details = jdbcTemplate.query(sql, new Object[] { orderId }, (rs, rowNum) -> {
            Map<String, Object> m = new HashMap<>();
            m.put("product", rs.getString("product_name"));
            m.put("quantity", rs.getBigDecimal("quantity_kg"));
            m.put("unitPrice", rs.getBigDecimal("unit_price"));
            return m;
        });

        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> d : details) {
            BigDecimal q = (BigDecimal) d.get("quantity");
            BigDecimal up = (BigDecimal) d.get("unitPrice");
            total = total.add(q.multiply(up));
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font h1 = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 11);

            Paragraph title = new Paragraph("Pedido - " + order.getOrderCode(), h1);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph(" "));

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.addCell(cell("Código:", normal));
            info.addCell(cell(order.getOrderCode(), normal));
            info.addCell(cell("Cliente:", normal));
            info.addCell(cell(c.getCompanyName(), normal));
            info.addCell(cell("Fecha:", normal));
            info.addCell(cell(order.getOrderDate().format(DATE_FMT), normal));
            info.addCell(cell("Estado:", normal));
            info.addCell(cell(order.getStatus(), normal));
            info.addCell(cell("Incoterm:", normal));
            info.addCell(cell(order.getIncoterm() == null ? "" : order.getIncoterm(), normal));
            doc.add(info);

            doc.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(new float[] { 4, 1, 2, 2 });
            table.setWidthPercentage(100);
            table.addCell(cell("Producto", normal));
            table.addCell(cell("Cant.", normal));
            table.addCell(cell("P. Unit.", normal));
            table.addCell(cell("Subtotal", normal));

            for (Map<String, Object> d : details) {
                String prod = (String) d.get("product");
                BigDecimal q = (BigDecimal) d.get("quantity");
                BigDecimal up = (BigDecimal) d.get("unitPrice");
                BigDecimal sub = q.multiply(up);
                table.addCell(cell(prod, normal));
                table.addCell(cell(q.toString(), normal));
                table.addCell(cell(up.toString(), normal));
                table.addCell(cell(sub.toString(), normal));
            }

            doc.add(table);
            doc.add(new Paragraph(" "));
            Paragraph pTotal = new Paragraph("Total: " + total.toString(), h1);
            pTotal.setAlignment(Element.ALIGN_RIGHT);
            doc.add(pTotal);

            doc.close();
            return baos.toByteArray();
        }
    }

    @Override
    public byte[] generateOrdersReportPdf() throws Exception {
        List<Order> orders = orderRepository.findAll();

        // totals per order
        String sqlTotals = "SELECT od.order_id, SUM(od.quantity_kg * od.unit_price) AS total FROM ORDER_DETAILS od GROUP BY od.order_id";
        Map<Integer, BigDecimal> totals = new HashMap<>();
        jdbcTemplate.query(sqlTotals, (ResultSet rs) -> {
            while (rs.next()) {
                totals.put(rs.getInt("order_id"), rs.getBigDecimal("total"));
            }
        });

        // count by status
        Map<String, Integer> countByStatus = new HashMap<>();
        for (Order o : orders) {
            countByStatus.put(o.getStatus(), countByStatus.getOrDefault(o.getStatus(), 0) + 1);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font h1 = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 11);

            doc.add(new Paragraph("Reporte de Pedidos", h1));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Total de pedidos: " + orders.size(), normal));
            for (Map.Entry<String, Integer> e : countByStatus.entrySet()) {
                doc.add(new Paragraph(e.getKey() + ": " + e.getValue(), normal));
            }

            doc.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(new float[] { 1, 3, 2, 2, 2 });
            table.setWidthPercentage(100);
            table.addCell(cell("ID", normal));
            table.addCell(cell("Código", normal));
            table.addCell(cell("Cliente", normal));
            table.addCell(cell("Fecha", normal));
            table.addCell(cell("Total", normal));

            for (Order o : orders) {
                String clientName = o.getCustomer() != null ? o.getCustomer().getCompanyName() : "";
                BigDecimal orderTotal = totals.getOrDefault(o.getOrderId(), BigDecimal.ZERO);
                table.addCell(cell(o.getOrderId().toString(), normal));
                table.addCell(cell(o.getOrderCode(), normal));
                table.addCell(cell(clientName, normal));
                table.addCell(cell(o.getOrderDate() == null ? "" : o.getOrderDate().format(DATE_FMT), normal));
                table.addCell(cell(orderTotal.toString(), normal));
            }

            doc.add(table);
            doc.close();
            return baos.toByteArray();
        }
    }

    private static PdfPCell cell(String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setPadding(6);
        return c;
    }
}
