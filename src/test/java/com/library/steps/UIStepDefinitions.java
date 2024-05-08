package com.library.steps;

import com.library.pages.*;
import com.library.utility.*;
import io.cucumber.java.en.*;

public class UIStepDefinitions {


    LoginPage loginPage = new LoginPage();
    BookPage bookPage = new BookPage();

    @Given("I logged in Library UI as {string}")
    public void i_logged_in_library_ui_as(String userType) {
        loginPage.login(userType);
        BrowserUtil.waitFor(3);
    }
    @Given("I navigate to {string} page")
    public void i_navigate_to_page(String pageName) {
        bookPage.navigateModule(pageName);
        BrowserUtil.waitFor(3);
    }
}