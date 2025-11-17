package org.lab.dev.web;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.DisabledOnIntegrationTest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lab.dev.utils.TestContainerResource;
import org.testcontainers.shaded.com.google.common.net.HttpHeaders;
import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ** New AssertJ Import **
import static org.assertj.core.api.Assertions.assertThat;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.post;
import static io.restassured.RestAssured.delete;

// Placeholder for your application's CartStatus enum
// enum CartStatus { NEW, CANCELED, DELIVERED }

//@DisabledOnNativeImage
@DisabledOnIntegrationTest
@QuarkusTest
@QuarkusTestResource(TestContainerResource.class)
//@QuarkusTestResource(KeycloakRealmResource.class)
class CartResourceTest {

    private static final String INSERT_WRONG_CART_IN_DB =
            "insert into carts values (9999, current_timestamp, current_timestamp, 'NEW', 3)";

    private static final String DELETE_WRONG_CART_IN_DB =
            "delete from carts where id = 9999";

    static String ADMIN_BEARER_TOKEN;
    static String USER_BEARER_TOKEN;

    @Inject
    DataSource dataSource;

    @BeforeAll
    static void init() {
        ADMIN_BEARER_TOKEN = System.getProperty("quarkus-admin-access-token");
        USER_BEARER_TOKEN = System.getProperty("quarkus-test-access-token");
    }

    // --- Admin Role Tests ---

    @Test
    void testFindAllWithAdminRole() {
        // Extract response to perform AssertJ assertions
        Response response = given().header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .when()
                .get("/carts")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract().response();

        // AssertJ: Check list size is greater than 0
        List<Map<String, Object>> carts = response.jsonPath().getList("$");
        assertThat(carts).isNotEmpty();
    }

    @Test
    void testFindAllActiveCartsWithAdminRole() {
        given().header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .when()
                .get("/carts/active")
                .then()
                .statusCode(Status.OK.getStatusCode());
    }

    @Test
    void testGetActiveCartForCustomerWithAdminRole() {
        Response response = given().header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .when()
                .get("/carts/customer/1")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract().response();

        // AssertJ: Check response body contains string
        assertThat(response.body().asString()).contains("Jason");
    }

    @Test
    void testGetActiveCartForCustomerWhenThereAreTwoCartsInDBWithAdminRole() {
        executeSql(INSERT_WRONG_CART_IN_DB);

        try {
            Response response = given().when()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                    .get("/carts/customer/3")
                    .then()
                    .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .extract().response();

            // AssertJ: Check response body contains strings
            String responseBody = response.body().asString();
            assertThat(responseBody).contains(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
            assertThat(responseBody).contains("Many active carts detected !!!");

        } finally {
            executeSql(DELETE_WRONG_CART_IN_DB);
        }
    }

    @Test
    void testFindByIdWithAdminRole() {
        // Test 1: Found cart
        Response response2 = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/carts/2")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract().response();

        // AssertJ: Check JSON body fields
        assertThat(response2.jsonPath().getString("status")).isEqualTo("NEW");
        assertThat(response2.body().asString()).contains("status"); // Check for key presence

        // Test 2: Not found cart (NO_CONTENT)
        Response response100 = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/carts/100")
                .then()
                .statusCode(Status.NO_CONTENT.getStatusCode())
                .extract().response();

        // AssertJ: Check body is empty (null or empty string)
        assertThat(response100.body().asString()).isBlank();
    }

    @Test
    void testCreateCartWithAdminRole() {
        var requestParams = new HashMap<String, String>();
        requestParams.put("firstName", "Saul");
        requestParams.put("lastName", "Berenson");
        requestParams.put("email", "call.saul@mail.com");

        // 1. Create Customer
        var newCustomerId = given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .body(requestParams)
                .post("/customers")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .jsonPath()
                .getInt("id");

        // AssertJ: Check newCustomerId is positive
        assertThat(newCustomerId).isPositive();

        // 2. Create Cart
        var response = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .post("/carts/customer/" + newCustomerId)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .jsonPath()
                .getMap("$");

        // AssertJ: Check map contents
        assertThat(response.get("id")).as("Cart ID").isNotNull();
        // Assuming CartStatus.NEW exists in your application
        // assertThat(response).containsEntry("status", CartStatus.NEW.name());
        assertThat(response).containsEntry("status", "NEW");


        // 3. Clean up Cart
        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/carts/" + response.get("id"))
                .then()
                .statusCode(Status.NO_CONTENT.getStatusCode());

        // 4. Clean up Customer
        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/customers/" + newCustomerId)
                .then()
                .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    // --- User Role Tests ---

    // (Similar AssertJ rewrites for User Role tests omitted for brevity,
    // applying the same pattern as the Admin tests above)

    @Test
    void testFindAllWithUserRole() {
        Response response = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .get("/carts")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract().response();

        List<Map<String, Object>> carts = response.jsonPath().getList("$");
        assertThat(carts).isNotEmpty();
    }

    @Test
    void testFindAllActiveCartsWithUserRole() {
        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .get("/carts/active")
                .then()
                .statusCode(Status.OK.getStatusCode());
    }

    @Test
    void testGetActiveCartForCustomerWithUserRole() {
        Response response = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .get("/carts/customer/3")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract().response();

        assertThat(response.body().asString()).contains("Peter");
    }

    @Test
    void testGetActiveCartForCustomerWhenThereAreTwoCartsInDBWithUserRole() {
        executeSql(INSERT_WRONG_CART_IN_DB);

        try {
            Response response = given().when()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                    .get("/carts/customer/3")
                    .then()
                    .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .extract().response();

            String responseBody = response.body().asString();
            assertThat(responseBody).contains(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
            assertThat(responseBody).contains("Many active carts detected !!!");

        } finally {
            executeSql(DELETE_WRONG_CART_IN_DB);
        }
    }

    @Test
    void testFindByIdWithUserRole() {
        // Test 1: Found cart
        Response response2 = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .get("/carts/2")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract().response();

        assertThat(response2.jsonPath().getString("status")).isEqualTo("NEW");
        assertThat(response2.body().asString()).contains("status");

        // Test 2: Not found cart (NO_CONTENT)
        Response response100 = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .get("/carts/100")
                .then()
                .statusCode(Status.NO_CONTENT.getStatusCode())
                .extract().response();

        assertThat(response100.body().asString()).isBlank();
    }

    @Test
    void testCreateCartWithUserRole() {
        var requestParams = new HashMap<String, String>();
        requestParams.put("firstName", "Saul");
        requestParams.put("lastName", "Berenson");
        requestParams.put("email", "call.saul@mail.com");

        var newCustomerId = given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .body(requestParams)
                .post("/customers")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .jsonPath()
                .getInt("id");

        var response = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .post("/carts/customer/" + newCustomerId)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat(response.get("id")).as("Cart ID").isNotNull();
        // Assuming CartStatus.NEW exists in your application
        // assertThat(response).containsEntry("status", CartStatus.NEW.name());
        assertThat(response).containsEntry("status", "NEW");

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .delete("/carts/" + response.get("id"))
                .then()
                .statusCode(Status.NO_CONTENT.getStatusCode());

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .delete("/customers/" + newCustomerId)
                .then()
                .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void testFailCreateCartWhileHavingAlreadyActiveCartWithUserRole() {
        var requestParams = new HashMap<String, String>();
        requestParams.put("firstName", "Saul");
        requestParams.put("lastName", "Berenson");
        requestParams.put("email", "call.saul@mail.com");

        // 1. Create Customer
        var newCustomerId = given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .body(requestParams)
                .post("/customers")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .jsonPath()
                .getLong("id");

        // 2. Create first Cart
        var newCartId = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .post("/carts/customer/" + newCustomerId)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .jsonPath()
                .getLong("id");

        // 3. Attempt to create second Cart (should fail)
        Response errorResponse = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .post("/carts/customer/" + newCustomerId)
                .then()
                .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .extract().response();

        String responseBody = errorResponse.body().asString();
        assertThat(responseBody).contains(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
        assertThat(responseBody).contains("There is already an active cart");

        // AssertJ: Check newCartId
        assertThat(newCartId).isNotZero();

        // 4. Clean up
        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .delete("/carts/" + newCartId)
                .then()
                .statusCode(Status.NO_CONTENT.getStatusCode());

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .delete("/customers/" + newCustomerId)
                .then()
                .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void testDeleteWithUserRole() {
        // 1. Verify cart is active
        Response getResponse1 = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .get("/carts/active")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract().response();

        String getBody1 = getResponse1.body().asString();
        assertThat(getBody1).contains("Peter");
        assertThat(getBody1).contains("NEW");


        // 2. Delete cart
        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .delete("/carts/3")
                .then()
                .statusCode(Status.NO_CONTENT.getStatusCode());

        // 3. Verify cart status is CANCELED
        Response getResponse2 = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_BEARER_TOKEN)
                .get("/carts/3")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract().response();

        String getBody2 = getResponse2.body().asString();
        assertThat(getBody2).contains("Peter");
        assertThat(getBody2).contains("CANCELED");
    }

    // --- Unauthorized/No Role Tests (unchanged as no body assertions needed) ---

    @Test
    void testFindAll() {
        get("/carts").then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testFindAllActiveCarts() {
        get("/carts/active").then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testGetActiveCartForCustomer() {
        get("/carts/customer/3").then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testFindById() {
        get("/carts/3").then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());

        get("/carts/100").then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testCreateCart() {
        post("/carts/customer/" + 555555).then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testDelete() {
        get("/carts/active").then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());

        delete("/carts/1").then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());

        get("/carts/1").then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    private void executeSql(String insertWrongCartInDb) {
        try (var connection = dataSource.getConnection()) {
            var statement = connection.createStatement();
            statement.executeUpdate(insertWrongCartInDb);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}