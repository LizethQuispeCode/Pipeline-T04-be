package vallegrande.edu.pe.visons;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.jdbc.core.JdbcTemplate;

import vallegrande.edu.pe.visons.dto.ClientRequestTransactionRequest;
import vallegrande.edu.pe.visons.dto.ClientRequestTransactionResponse;
import vallegrande.edu.pe.visons.dto.OrderDTO;
import vallegrande.edu.pe.visons.dto.OrderDetailDTO;
import vallegrande.edu.pe.visons.dto.OrderResponseDTO;
import vallegrande.edu.pe.visons.model.Product;
import vallegrande.edu.pe.visons.rest.OrderRest;
import vallegrande.edu.pe.visons.service.ClientRequestService;
import vallegrande.edu.pe.visons.service.ProductService;

@SpringBootTest
class VisonsApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private OrderRest orderRest;

	@Autowired
	private ProductService productService;

	@Autowired
	private ClientRequestService clientRequestService;

	@BeforeEach
	void cleanDatabase() {
		jdbcTemplate.update("DELETE FROM ORDER_DETAILS");
		jdbcTemplate.update("DELETE FROM ORDERS");
		jdbcTemplate.update("DELETE FROM CURRENT_INVENTORY");
		jdbcTemplate.update("DELETE FROM PRODUCTS");
		jdbcTemplate.update("DELETE FROM CATEGORIES");
		jdbcTemplate.update("DELETE FROM CLIENT_REQUESTS");
		jdbcTemplate.update("DELETE FROM UBIGEO");
		jdbcTemplate.update("DELETE FROM CLIENTS");
	}

	@Test
	void contextLoads() {
	}

	@Test
	void createOrderReservesCurrentInventoryStock() {
		Integer clientId = createClient();
		Integer productId = createProductWithInventory(new BigDecimal("100.000"));

		OrderDTO order = new OrderDTO(
				clientId,
				"ORD-STOCK-001",
				LocalDate.now(),
				"FOB",
				"Pending",
				List.of(new OrderDetailDTO(productId, null, new BigDecimal("12.500"), BigDecimal.ZERO, null)));

		orderRest.createOrder(order);

		BigDecimal totalStock = jdbcTemplate.queryForObject(
				"SELECT total_stock_kg FROM CURRENT_INVENTORY WHERE product_id = ?",
				BigDecimal.class,
				productId);
		BigDecimal reservedStock = jdbcTemplate.queryForObject(
				"SELECT reserved_stock_kg FROM CURRENT_INVENTORY WHERE product_id = ?",
				BigDecimal.class,
				productId);
		BigDecimal availableStock = jdbcTemplate.queryForObject(
				"SELECT available_stock_kg FROM CURRENT_INVENTORY WHERE product_id = ?",
				BigDecimal.class,
				productId);

		assertThat(totalStock).isEqualByComparingTo("100.000");
		assertThat(reservedStock).isEqualByComparingTo("12.500");
		assertThat(availableStock).isEqualByComparingTo("87.500");
	}

	@Test
	void acceptOrderConsumesReservedStockAndKeepsAvailableStockConsistent() {
		Integer workerUserId = createWorkerUser();
		Integer clientId = createClient();
		Integer productId = createProductWithInventory(new BigDecimal("100.000"));

		OrderDTO order = new OrderDTO(
				clientId,
				"ORD-STOCK-002",
				LocalDate.now(),
				"FOB",
				"Pending",
				List.of(new OrderDetailDTO(productId, null, new BigDecimal("12.500"), BigDecimal.ZERO, null)));

		OrderResponseDTO createdOrder = (OrderResponseDTO) orderRest.createOrder(order).getBody();
		MockHttpSession session = authenticatedWorkerSession(workerUserId);

		orderRest.acceptOrder(createdOrder.getOrderId(), session);

		BigDecimal totalStock = jdbcTemplate.queryForObject(
				"SELECT total_stock_kg FROM CURRENT_INVENTORY WHERE product_id = ?",
				BigDecimal.class,
				productId);
		BigDecimal reservedStock = jdbcTemplate.queryForObject(
				"SELECT reserved_stock_kg FROM CURRENT_INVENTORY WHERE product_id = ?",
				BigDecimal.class,
				productId);
		BigDecimal availableStock = jdbcTemplate.queryForObject(
				"SELECT available_stock_kg FROM CURRENT_INVENTORY WHERE product_id = ?",
				BigDecimal.class,
				productId);

		assertThat(totalStock).isEqualByComparingTo("87.500");
		assertThat(reservedStock).isEqualByComparingTo("0.000");
		assertThat(availableStock).isEqualByComparingTo("87.500");
	}

	@Test
	void rejectOrderReleasesReservedStockWithoutChangingTotalStock() {
		Integer workerUserId = createWorkerUser();
		Integer clientId = createClient();
		Integer productId = createProductWithInventory(new BigDecimal("100.000"));

		OrderDTO order = new OrderDTO(
				clientId,
				"ORD-STOCK-003",
				LocalDate.now(),
				"FOB",
				"Pending",
				List.of(new OrderDetailDTO(productId, null, new BigDecimal("12.500"), BigDecimal.ZERO, null)));

		OrderResponseDTO createdOrder = (OrderResponseDTO) orderRest.createOrder(order).getBody();
		MockHttpSession session = authenticatedWorkerSession(workerUserId);

		orderRest.rejectOrder(createdOrder.getOrderId(), session);

		BigDecimal totalStock = jdbcTemplate.queryForObject(
				"SELECT total_stock_kg FROM CURRENT_INVENTORY WHERE product_id = ?",
				BigDecimal.class,
				productId);
		BigDecimal reservedStock = jdbcTemplate.queryForObject(
				"SELECT reserved_stock_kg FROM CURRENT_INVENTORY WHERE product_id = ?",
				BigDecimal.class,
				productId);
		BigDecimal availableStock = jdbcTemplate.queryForObject(
				"SELECT available_stock_kg FROM CURRENT_INVENTORY WHERE product_id = ?",
				BigDecimal.class,
				productId);

		assertThat(totalStock).isEqualByComparingTo("100.000");
		assertThat(reservedStock).isEqualByComparingTo("0.000");
		assertThat(availableStock).isEqualByComparingTo("100.000");
	}

	@Test
	void createProductCreatesCurrentInventoryWithInitialStock() {
		Integer categoryId = createCategory("Citricos");
		Product product = new Product();
		product.setCategoryId(categoryId);
		product.setName("Limon Test");
		product.setVariety("Tahiti");
		product.setCaliber("Pequeno");
		product.setUnitMeasure("KG");
		product.setBoxWeightKg(new BigDecimal("12.00"));
		product.setInitialStockKg(new BigDecimal("42.250"));
		product.setIsOwnProduction(false);
		product.setIsActive(true);

		Product savedProduct = productService.save(product);

		BigDecimal totalStock = jdbcTemplate.queryForObject(
				"SELECT total_stock_kg FROM CURRENT_INVENTORY WHERE product_id = ?",
				BigDecimal.class,
				savedProduct.getProductId());
		BigDecimal availableStock = jdbcTemplate.queryForObject(
				"SELECT available_stock_kg FROM CURRENT_INVENTORY WHERE product_id = ?",
				BigDecimal.class,
				savedProduct.getProductId());

		assertThat(savedProduct.getTotalStockKg()).isEqualByComparingTo("42.250");
		assertThat(totalStock).isEqualByComparingTo("42.250");
		assertThat(availableStock).isEqualByComparingTo("42.250");
	}

	@Test
	void createClientRequestTransactionDefaultsAndLists() {
		Integer ubigeoId = createUbigeo();
		ClientRequestTransactionRequest request = buildClientRequest("cliente.norte", "cliente.norte@test.pe");
		request.setCountry("");
		request.setUbigeoId(ubigeoId);

		ClientRequestTransactionResponse savedRequest = clientRequestService.save(request);

		assertThat(savedRequest.getRequestId()).isNotNull();
		assertThat(savedRequest.getTransactionCode()).isEqualTo("CR-%06d".formatted(savedRequest.getRequestId()));
		assertThat(savedRequest.getStatus()).isEqualTo("Pending");
		assertThat(savedRequest.getCountry()).isEqualTo("Peru");
		assertThat(savedRequest.getRequestDate()).isNotNull();
		assertThat(savedRequest.getReviewedBy()).isNull();
		assertThat(savedRequest.getUbigeoId()).isEqualTo(ubigeoId);

		Integer storedRows = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM CLIENT_REQUESTS WHERE request_id = ? AND status = ?",
				Integer.class,
				savedRequest.getRequestId(),
				"Pending");
		assertThat(storedRows).isEqualTo(1);

		assertThat(clientRequestService.findAll())
				.extracting(ClientRequestTransactionResponse::getRequestId)
				.contains(savedRequest.getRequestId());
		assertThat(clientRequestService.findByStatus("Pending"))
				.extracting(ClientRequestTransactionResponse::getRequestId)
				.contains(savedRequest.getRequestId());
	}

	@Test
	void createClientRequestTransactionRejectsInvalidUbigeo() {
		ClientRequestTransactionRequest request = buildClientRequest("cliente.invalid", "cliente.invalid@test.pe");
		request.setUbigeoId(99999);

		assertThatThrownBy(() -> clientRequestService.save(request))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("ubigeoId");
	}

	private Integer createClient() {
		jdbcTemplate.update(
				"INSERT INTO CLIENTS (company_name, tax_id, country, address, email, credit_limit, is_active) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?)",
				"Cliente Test",
				"20999999999",
				"Peru",
				"Av. Test 123",
				"cliente@test.pe",
				BigDecimal.ZERO,
				true);
		return jdbcTemplate.queryForObject("SELECT client_id FROM CLIENTS WHERE tax_id = ?", Integer.class, "20999999999");
	}

	private Integer createProductWithInventory(BigDecimal stockKg) {
		Integer categoryId = createCategory("Frutas");

		jdbcTemplate.update(
				"INSERT INTO PRODUCTS (category_id, name, variety, caliber, unit_measure, box_weight_kg, is_own_production, is_active) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
				categoryId,
				"Mango Test",
				"Kent",
				"Grande",
				"KG",
				new BigDecimal("10.00"),
				true,
				true);
		Integer productId = jdbcTemplate.queryForObject("SELECT product_id FROM PRODUCTS WHERE name = ?",
				Integer.class,
				"Mango Test");

		jdbcTemplate.update(
				"INSERT INTO CURRENT_INVENTORY (product_id, total_stock_kg, reserved_stock_kg, available_stock_kg) "
						+ "VALUES (?, ?, ?, ?)",
				productId,
				stockKg,
				BigDecimal.ZERO,
				stockKg);
		return productId;
	}

	private Integer createCategory(String name) {
		jdbcTemplate.update("INSERT INTO CATEGORIES (name, description, is_active) VALUES (?, ?, ?)",
				name,
				"Categoria de prueba",
				true);
		return jdbcTemplate.queryForObject("SELECT category_id FROM CATEGORIES WHERE name = ?",
				Integer.class,
				name);
	}

	private Integer createUbigeo() {
		jdbcTemplate.update(
				"INSERT INTO UBIGEO (ubigeo_code, department, province, district, is_active) VALUES (?, ?, ?, ?, ?)",
				"150101",
				"Lima",
				"Lima",
				"Lima",
				true);
		return jdbcTemplate.queryForObject("SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code = ?",
				Integer.class,
				"150101");
	}

	private ClientRequestTransactionRequest buildClientRequest(String username, String email) {
		ClientRequestTransactionRequest request = new ClientRequestTransactionRequest();
		request.setUsername(username);
		request.setFirstName("Valeria");
		request.setLastName("Campos");
		request.setCompanyName("Agro Norte SAC");
		request.setTaxId("20700123456");
		request.setCountry("Peru");
		request.setEmail(email);
		request.setPhone("987111222");
		request.setAddress("Av. Grau 150");
		request.setComments("Solicitud de prueba");
		return request;
	}

	private Integer createWorkerUser() {
		Integer workerId = createWorker();
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS USER_ROLES (user_id INT, role_id INT, PRIMARY KEY (user_id, role_id))");
		jdbcTemplate.update("INSERT INTO USER_TYPES (name) SELECT ? WHERE NOT EXISTS (SELECT 1 FROM USER_TYPES WHERE name = ?)",
				"EMPLOYEE",
				"EMPLOYEE");
		Integer userTypeId = jdbcTemplate.queryForObject(
				"SELECT id FROM USER_TYPES WHERE name = ?",
				Integer.class,
				"EMPLOYEE");
		String uniqueSuffix = String.valueOf(System.nanoTime());

		jdbcTemplate.update(
				"INSERT INTO USERS (username, password_hash, user_type_id, worker_id, client_id, is_active) VALUES (?, ?, ?, ?, ?, ?)",
				"worker.stock.test." + uniqueSuffix,
				"hash",
				userTypeId,
				workerId,
				null,
				true);
		return jdbcTemplate.queryForObject("SELECT user_id FROM USERS WHERE username = ?", Integer.class,
				"worker.stock.test." + uniqueSuffix);
	}

	private Integer createWorker() {
		Integer ubigeoId = createUbigeo();
		String uniqueSuffix = String.valueOf(System.nanoTime());
		String documentNumber = "W" + uniqueSuffix;
		jdbcTemplate.update(
				"INSERT INTO WORKERS (first_name, last_name, phone, email, address, ubigeo_id, document_type, document_number, hire_date, status, is_active) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				"Worker",
				"Test",
				"999111222",
				"worker@test.pe",
				"Av. Trabajo 123",
				ubigeoId,
				"DNI",
				documentNumber,
				LocalDate.now(),
				"ACTIVE",
				true);
		return jdbcTemplate.queryForObject("SELECT worker_id FROM WORKERS WHERE document_number = ?", Integer.class,
				documentNumber);
	}

	private MockHttpSession authenticatedWorkerSession(Integer userId) {
		MockHttpSession session = new MockHttpSession();
		session.setAttribute("VISONS_CURRENT_USER_ID", userId);
		return session;
	}
}
