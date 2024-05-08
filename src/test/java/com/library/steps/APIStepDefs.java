package com.library.steps;


import com.library.pages.*;
import com.library.utility.*;
import io.cucumber.java.en.*;
import io.restassured.*;
import io.restassured.http.*;
import io.restassured.path.json.*;
import io.restassured.response.*;
import io.restassured.specification.*;
import org.junit.*;

import org.hamcrest.Matchers;


import java.util.*;

import static io.restassured.path.json.JsonPath.given;
import static org.hamcrest.Matchers.*;

public class APIStepDefs {

    RequestSpecification givenPart;
    ValidatableResponse thenPart;
    Response response;

    String id;

    Map<String, Object> randomDataMap;

    @Given("I logged Library api as a {string}")
    public void i_logged_library_api_as_a(String userType) {
        givenPart = RestAssured.given()
                .header("x-library-token", LibraryAPI_Util.getToken(userType));
    }

    @Given("Accept header is {string}")
    public void accept_header_is(String contentType) {
        givenPart.accept(contentType);
    }

    @When("I send GET request to {string} endpoint")
    public void i_send_get_request_to_endpoint(String endPoint) {
        response = givenPart.when().get(ConfigurationReader.getProperty("library.baseUri") + endPoint);
        thenPart = response.then();
    }

    @Then("status code should be {int}")
    public void status_code_should_be(int statusCode) {
        thenPart.statusCode(statusCode);
    }

    @Then("Response Content type is {string}")
    public void response_content_type_is(String contentType) {
        thenPart.contentType(contentType);
    }

    @Then("Each {string} field should not be null")
    public void each_field_should_not_be_null(String path) {
        thenPart.body(path, everyItem(notNullValue()));

    }

    @Given("Path param is {string}")
    public void path_param_is(String value) {
        givenPart.pathParam("id", value);
        id = value;
    }

    @Then("{string} field should be same with path param")
    public void field_should_be_same_with_path_param(String path) {
        thenPart.body(path, is(id));
    }

    @Then("following fields should not be null")
    public void following_fields_should_not_be_null(List<String> expectedData) {
        for (String data : expectedData) {
            thenPart.body(data, is(notNullValue()));
        }
    }

    @Given("Request Content Type header is {string}")
    public void request_content_type_header_is(String contentType) {
        givenPart.contentType(contentType);
    }

    @Given("I create a random {string} as request body")
    public void i_create_a_random_as_request_body(String randomData) {
        Map<String, Object> requestMap;
        switch (randomData) {

            case "user":
                requestMap = LibraryAPI_Util.getRandomUserMap();
                break;
            case "book":
                requestMap = LibraryAPI_Util.getRandomBookMap();
                break;
            default:
                throw new RuntimeException("Unexpected Value" + randomData);
        }
        System.out.println("requestMap = " + requestMap);

        //Map<String, Object> randomDataMap;
        randomDataMap = requestMap;
        givenPart.formParams(randomDataMap);
    }

    @When("I send POST request to {string} endpoint")
    public void i_send_post_request_to_endpoint(String endPoint) {
        response = givenPart.when()
                .post(ConfigurationReader.getProperty("library.baseUri") + endPoint).prettyPeek();
        thenPart = response.then();
    }

    @Then("the field value for {string} path should be equal to {string}")
    public void the_field_value_for_path_should_be_equal_to(String path, String value) {
        thenPart.body(path, is(value));
    }

    @Then("{string} field should not be null")
    public void field_should_not_be_null(String path) {
        thenPart.body(path, is(notNullValue()));
    }

    @Then("UI, Database and API created book information must match")
    public void ui_database_and_api_created_book_information_must_match() {
        /*
        bookname
        year
        isbn
        author
        category
        description
         */

        /*
        1. create a map about book information from api  --> bookMapApi
        2. create a map about book information from db   --> bookMapDb
        3. create a map about book information from ui   --> bookMapUi

        assertEquals(bookMapApi,bookMapDb)
        assertEquals(bookMapApi,bookMapUi)
         */

        // get book info from api
        String id = response.path("book_id");
        System.out.println("newly created book id = " + id);

        Response response1 = RestAssured.given().accept(ContentType.JSON)
                .header("x-library-token", LibraryAPI_Util.getToken("librarian"))
                .and().pathParam("id", id)
                .when().get(ConfigurationReader.getProperty("library.baseUri") + "/get_book_by_id/{id}");

        JsonPath jsonPath = response1.jsonPath();
        Map<String, Object> bookMapApi = new LinkedHashMap<>();

        bookMapApi.put("name", jsonPath.getString(("name")));
        bookMapApi.put("isbn", jsonPath.getString(("isbn")));
        bookMapApi.put("year", jsonPath.getString(("year")));
        bookMapApi.put("author", jsonPath.getString(("author")));
        bookMapApi.put("book_category_id", jsonPath.getString(("book_category_id")));
        bookMapApi.put("description", jsonPath.getString(("description")));

        System.out.println("bookMapApi = " + bookMapApi);


        // get book info from DB
        DB_Util.runQuery("select * from books where id = " + id);
        Map<String, Object> bookMapDb = DB_Util.getRowMap(1);

        System.out.println("bookMapDb = " + bookMapDb);

        bookMapDb.remove("id");
        bookMapDb.remove("added_date");

        System.out.println("bookMapDb = " + bookMapDb);


        //get map from ui
        String bookName = (String) randomDataMap.get("name");
        BookPage bookPage = new BookPage();
        bookPage.search.sendKeys(bookName);
        BrowserUtil.waitFor(3);

        bookPage.editBook(bookName).click();
        BrowserUtil.waitFor(3);

        Map<String, Object> bookMapUi = new LinkedHashMap<>();

        String currentBookName = bookPage.bookName.getAttribute("value");
        bookMapUi.put("name", currentBookName);

        String isbn = bookPage.isbn.getAttribute("value");
        bookMapUi.put("isbn", isbn);

        String year = bookPage.year.getAttribute("value");
        bookMapUi.put("year", year);

        String author = bookPage.author.getAttribute("value");
        bookMapUi.put("author", author);

        String selectedBookCategoryName = BrowserUtil.getSelectedOption(bookPage.categoryDropdown);
        DB_Util.runQuery("select id from book_categories where name='" + selectedBookCategoryName + "'");
        String uiCategoryID = DB_Util.getFirstRowFirstColumn();
        bookMapUi.put("book_category_id", uiCategoryID);

        String description = bookPage.description.getAttribute("value");
        bookMapUi.put("description", description);

        System.out.println("bookMapUi = " + bookMapUi);

        Assert.assertEquals(bookMapApi, bookMapDb);
        Assert.assertEquals(bookMapApi, bookMapUi);


    }

    @Then("created user information should match with Database")
    public void created_user_information_should_match_with_database() {

        String userID = response.path("user_id");
        System.out.println("User is generated with following ID = " + userID);
        String query = "select full_name,email,user_group_id,status,start_date,end_date,address from users where id =" + userID + "";
        DB_Util.runQuery(query);

        //ACTUAL -->Database stores this data as expected
        Map<String, Object> dbUser = DB_Util.getRowMap(1);

        System.out.println("dbUSer information is retrieved from database = " + dbUser);

        //EXPECTED  -->POST Data from API
        System.out.println("POSTED Data from API = " + randomDataMap);

        //API UserData has additional Password and it is nor possible to compare since it encrypted
        String password = (String) randomDataMap.remove("password");

        Assert.assertEquals(randomDataMap, dbUser);
        randomDataMap.put("password", password);
    }

    @Then("created user should be able to login Library UI")
    public void created_user_should_be_able_to_login_library_ui() {
        LoginPage loginPage = new LoginPage();
        //Get credentials from created user
        String email = (String) randomDataMap.get("email");
        System.out.println("Created user trying to login = " + email);
        String password = (String) randomDataMap.get("password");
        System.out.println("Created user trying to login = " + password);
        loginPage.login(email, password);
        BrowserUtil.waitFor(2);
    }

    @Then("created user name should appear in Dashboard Page")
    public void created_user_name_should_appear_in_dashboard_page() {

        BookPage bookPage = new BookPage();
        BrowserUtil.waitFor(2);
        String uiFullNameActual = bookPage.accountHolderName.getText();
        String APIFullNameExpected = (String) randomDataMap.get("full_name");
        Assert.assertEquals(APIFullNameExpected, uiFullNameActual);

    }

    String token;

    @Given("I logged Library api with credentials {string} and {string}")
    public void i_logged_library_api_with_credentials_and(String email, String password) {

        token = LibraryAPI_Util.getToken(email, password);
        givenPart = RestAssured.given().log().uri();
    }

    @Given("I send token information as request body")
    public void i_send_token_information_as_request_body() {
        givenPart.formParams("token", token);
    }
}