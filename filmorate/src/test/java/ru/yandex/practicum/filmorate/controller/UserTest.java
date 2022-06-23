package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.adapter.LocalDateDeserializer;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

class UserTest {
    private String url = "http://localhost:8080/users";
    private RestTemplate restTemplate = new RestTemplate();
    private Gson gson = makeGson();

    Gson makeGson() {
        GsonBuilder gsonBuilder  = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        gson = gsonBuilder.serializeNulls().setPrettyPrinting().create();
        return gson;
    }

    @Test
    void postAndGetUser() {
        ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);

        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name");
        HttpEntity<User> request = new HttpEntity<>(user);

        // POST request
        restTemplate.exchange(url,
                HttpMethod.POST,
                request,
                String.class);

        // GET request
        ResponseEntity<Collection> response = restTemplate.getForEntity(url, Collection.class);
        Collection<User> users = response.getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=name}]",
                users.toString());

        ctx.close();
    }

    @Test
    void postPutAndGetFilm() {
        ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name");

        // POST request
        HttpEntity<User> request = new HttpEntity<>(user);
        String productCreateResponse = restTemplate.postForObject(url, request, String.class);

        User createdUser = gson.fromJson(productCreateResponse, User.class);
        createdUser.setLogin("updatedLogin");
        createdUser.setName("updatedName");

        // PUT request
        HttpEntity<User> newRequest = new HttpEntity<>(createdUser);
        restTemplate.exchange(
                url,
                HttpMethod.PUT,
                newRequest,
                String.class);

        // GET request
        ResponseEntity<Collection> response = restTemplate.getForEntity(url, Collection.class);
        Collection<User> users = response.getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=updatedLogin, birthday=2022-06-21, " +
                        "name=updatedName}]",
                users.toString());

        ctx.close();
    }

    @Test
    void shouldNotPostWithEmptyEmail() {
        ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
        User user = new User("", "login",
                LocalDate.of(2022, 6, 21), "name");

        try {
            HttpEntity<User> request = new HttpEntity<>(user);

            // POST request
            restTemplate.exchange(url,
                    HttpMethod.POST,
                    request,
                    String.class);
        } catch (Throwable ignored){}

        // GET request
        ResponseEntity<Collection> response = restTemplate.getForEntity(url, Collection.class);
        Collection<User> users = response.getBody();

        Assertions.assertEquals("[]",
                users.toString());

        ctx.close();
    }

    @Test
    void shouldNotPostWithoutSymbolInEmail() {
        ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
        User user = new User("emailru", "login",
                LocalDate.of(2022, 6, 21), "name");

        try {
            HttpEntity<User> request = new HttpEntity<>(user);

            // POST request
            restTemplate.exchange(url,
                    HttpMethod.POST,
                    request,
                    String.class);
        } catch (Throwable ignored){}

        // GET request
        ResponseEntity<Collection> response = restTemplate.getForEntity(url, Collection.class);
        Collection<User> users = response.getBody();

        Assertions.assertEquals("[]",
                users.toString());

        ctx.close();
    }

    @Test
    void shouldNotPostWithEmptyLogin() {
        ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
        User user = new User("email@ru", "",
                LocalDate.of(2022, 6, 21), "name");

        try {
            HttpEntity<User> request = new HttpEntity<>(user);

            // POST request
            restTemplate.exchange(url,
                    HttpMethod.POST,
                    request,
                    String.class);
        } catch (Throwable ignored){}

        // GET request
        ResponseEntity<Collection> response = restTemplate.getForEntity(url, Collection.class);
        Collection<User> users = response.getBody();

        Assertions.assertEquals("[]",
                users.toString());

        ctx.close();
    }

    @Test
    void shouldNotPostWithSpaceInLogin() {
        ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
        User user = new User("email@ru", "lo gin",
                LocalDate.of(2022, 6, 21), "name");

        try {
            HttpEntity<User> request = new HttpEntity<>(user);

            // POST request
            restTemplate.exchange(url,
                    HttpMethod.POST,
                    request,
                    String.class);
        } catch (Throwable ignored){}

        // GET request
        ResponseEntity<Collection> response = restTemplate.getForEntity(url, Collection.class);
        Collection<User> users = response.getBody();

        Assertions.assertEquals("[]",
                users.toString());

        ctx.close();
    }

    @Test
    void shouldPostWithoutName() {
        ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "");

        try {
            HttpEntity<User> request = new HttpEntity<>(user);

            // POST request
            restTemplate.exchange(url,
                    HttpMethod.POST,
                    request,
                    String.class);
        } catch (Throwable ignored){}

        // GET request
        ResponseEntity<Collection> response = restTemplate.getForEntity(url, Collection.class);
        Collection<User> users = response.getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=login}]",
                users.toString());

        ctx.close();
    }

    @Test
    void shouldNotPostWithBirthdayInFuture() {
        ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
        User user = new User("email@ru", "login",
                LocalDate.of(2122, 6, 21), "name");

        try {
            HttpEntity<User> request = new HttpEntity<>(user);

            // POST request
            restTemplate.exchange(url,
                    HttpMethod.POST,
                    request,
                    String.class);
        } catch (Throwable ignored){}

        // GET request
        ResponseEntity<Collection> response = restTemplate.getForEntity(url, Collection.class);
        Collection<User> users = response.getBody();

        Assertions.assertEquals("[]",
                users.toString());

        ctx.close();
    }

    @Test
    void shouldPostAndNotPutWithEmptyEmail() {
        ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name");

        try {
            // POST request
            HttpEntity<User> request = new HttpEntity<>(user);
            String productCreateResponse = restTemplate.postForObject(url, request, String.class);
            user = gson.fromJson(productCreateResponse, User.class);
        } catch (Throwable ignored){}

        // PUT request
        user.setEmail("");
        try	{
            HttpEntity<User> newRequest = new HttpEntity<>(user);
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    newRequest,
                    String.class);
        } catch (Throwable ignored){}

        // GET request
        ResponseEntity<Collection> response = restTemplate.getForEntity(url, Collection.class);
        Collection<User> users = response.getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=name}]",
                users.toString());

        ctx.close();
    }

    @Test
    void shouldPostAndNotPutWithoutSymbolInEmail() {
        ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name");

        try {
            // POST request
            HttpEntity<User> request = new HttpEntity<>(user);
            String productCreateResponse = restTemplate.postForObject(url, request, String.class);
            user = gson.fromJson(productCreateResponse, User.class);
        } catch (Throwable ignored){}

        // PUT request
        user.setEmail("email");
        try	{
            HttpEntity<User> newRequest = new HttpEntity<>(user);
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    newRequest,
                    String.class);
        } catch (Throwable ignored){}

        // GET request
        ResponseEntity<Collection> response = restTemplate.getForEntity(url, Collection.class);
        Collection<User> users = response.getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=name}]",
                users.toString());

        ctx.close();
    }

    @Test
    void shouldPostAndNotPutWithEmptyLogin() {
        ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name");

        try {
            // POST request
            HttpEntity<User> request = new HttpEntity<>(user);
            String productCreateResponse = restTemplate.postForObject(url, request, String.class);
            user = gson.fromJson(productCreateResponse, User.class);
        } catch (Throwable ignored){}

        // PUT request
        user.setLogin("");
        try	{
            HttpEntity<User> newRequest = new HttpEntity<>(user);
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    newRequest,
                    String.class);
        } catch (Throwable ignored){}

        // GET request
        ResponseEntity<Collection> response = restTemplate.getForEntity(url, Collection.class);
        Collection<User> users = response.getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=name}]",
                users.toString());

        ctx.close();
    }

    @Test
    void shouldPostAndNotPutWithSpaceInLogin() {
        ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name");

        try {
            // POST request
            HttpEntity<User> request = new HttpEntity<>(user);
            String productCreateResponse = restTemplate.postForObject(url, request, String.class);
            user = gson.fromJson(productCreateResponse, User.class);
        } catch (Throwable ignored){}

        // PUT request
        user.setLogin("lo gin");
        try	{
            HttpEntity<User> newRequest = new HttpEntity<>(user);
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    newRequest,
                    String.class);
        } catch (Throwable ignored){}

        // GET request
        ResponseEntity<Collection> response = restTemplate.getForEntity(url, Collection.class);
        Collection<User> users = response.getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=name}]",
                users.toString());

        ctx.close();
    }

    @Test
    void shouldPostAndPutWithoutName() {
        ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name");

        try {
            // POST request
            HttpEntity<User> request = new HttpEntity<>(user);
            String productCreateResponse = restTemplate.postForObject(url, request, String.class);
            user = gson.fromJson(productCreateResponse, User.class);
        } catch (Throwable ignored){}

        // PUT request
        user.setName("");
        try	{
            HttpEntity<User> newRequest = new HttpEntity<>(user);
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    newRequest,
                    String.class);
        } catch (Throwable ignored){}

        // GET request
        ResponseEntity<Collection> response = restTemplate.getForEntity(url, Collection.class);
        Collection<User> users = response.getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=login}]",
                users.toString());

        ctx.close();
    }

    @Test
    void shouldPostAndNotPutWithBirthdayInFuture() {
        ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name");

        try {
            // POST request
            HttpEntity<User> request = new HttpEntity<>(user);
            String productCreateResponse = restTemplate.postForObject(url, request, String.class);
            user = gson.fromJson(productCreateResponse, User.class);
        } catch (Throwable ignored){}

        // PUT request
        user.setBirthday(LocalDate.of(2122, 6, 21));
        try	{
            HttpEntity<User> newRequest = new HttpEntity<>(user);
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    newRequest,
                    String.class);
        } catch (Throwable ignored){}

        // GET request
        ResponseEntity<Collection> response = restTemplate.getForEntity(url, Collection.class);
        Collection<User> users = response.getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=name}]",
                users.toString());

        ctx.close();
    }
}