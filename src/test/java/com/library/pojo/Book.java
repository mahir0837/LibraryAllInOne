package com.library.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {
    private String name;
    private String isbn;
    private String author;
    private int year;
    private int book_category_id;
    private String description;

    public static Map<String, Object> convertPojoToMap(Book book) {
        Map<String, Object> bookMap = new LinkedHashMap<>();
        Class<?> bookClass = book.getClass();
        for (Field field : bookClass.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                bookMap.put(field.getName(), field.get(book));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return bookMap;
    }
}
