package com.library.pages;

import com.library.utility.Driver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;

public class BookPage extends BasePage {
    WebElement element;

    @FindBy(xpath = "//table/tbody/tr")
    public List<WebElement> allRows;

    @FindBy(xpath = "//input[@type='search']")
    public WebElement search;

    @FindBy(id = "book_categories")
    public WebElement mainCategoryElement;

    @FindBy(name = "name")
    public WebElement bookName;


    @FindBy(xpath = "(//input[@type='text'])[4]")
    public WebElement author;

    @FindBy(xpath = "//div[@class='portlet-title']//a")
    public WebElement addBook;

    @FindBy(xpath = "//button[@type='submit']")
    public WebElement saveChanges;

    @FindBy(xpath = "//div[@class='toast-message']")
    public WebElement toastMessage;

    @FindBy(name = "year")
    public WebElement year;

    @FindBy(name = "isbn")
    public WebElement isbn;

    @FindBy(id = "book_group_id")
    public WebElement categoryDropdown;


    @FindBy(id = "description")
    public WebElement description;


    public WebElement editBook(String book) {
        String xpath = "//td[3][.='" + book + "']/../td/a";
        return Driver.getDriver().findElement(By.xpath(xpath));
    }

    public WebElement borrowBook(String book) {
        String xpath = "//td[3][.='" + book + "']/../td/a";
        return Driver.getDriver().findElement(By.xpath(xpath));
    }

    public List<String> getBookInformation() {
        String path = "(//div[@id='tbl_books_wrapper']//td)";
        List<String> bookInfo = new ArrayList<>();
        for (int i = 2; i <= 6; i++) {
            if (i != 5) {
                element.findElement(By.xpath(path + "[" + i + "]"));
                bookInfo.add(element.getText());
            }
        }
        return bookInfo;
    }


}
