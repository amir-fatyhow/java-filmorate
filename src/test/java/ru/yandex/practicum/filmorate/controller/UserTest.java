package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
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
    private ConfigurableApplicationContext ctx;

    @BeforeEach
    void runSpringApplication() {
        ctx = SpringApplication.run(FilmorateApplication.class);
    }

    @AfterEach
    void closeSpringApplication() {
        ctx.close();
    }

    @Test
    @DisplayName("Should post user with correct data and get film")
    void postAndGetUser() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name");

        makePostRequest(user);

        Collection<User> users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=name}]",
                users.toString());
    }

    @Test
    @DisplayName("Should post user with correct data then post with another login, name and get user")
    void postPutAndGetFilm() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name");

        User createdUser = returnCreatedFilmAfterPostRequest(user);
        createdUser.setLogin("updatedLogin");
        createdUser.setName("updatedName");

        makePutRequest(createdUser);

        Collection<User> users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=updatedLogin, birthday=2022-06-21, " +
                        "name=updatedName}]",
                users.toString());
    }

    @Test
    void shouldNotPostWithEmptyEmail() {
        User user = new User("", "login",
                LocalDate.of(2022, 6, 21), "name");

        try {
            makePostRequest(user);
        } catch (Throwable ignored){}

        Collection<User> users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[]",
                users.toString());
    }

    @Test
    void shouldNotPostWithoutSymbolInEmail() {
        User user = new User("emailru", "login",
                LocalDate.of(2022, 6, 21), "name");

        try {
            makePostRequest(user);
        } catch (Throwable ignored){}

        Collection<User> users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[]",
                users.toString());
    }

    @Test
    void shouldNotPostWithEmptyLogin() {
        User user = new User("email@ru", "",
                LocalDate.of(2022, 6, 21), "name");

        try {
            makePostRequest(user);
        } catch (Throwable ignored){}

        Collection<User> users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[]",
                users.toString());
    }

    @Test
    void shouldNotPostWithSpaceInLogin() {
        User user = new User("email@ru", "lo gin",
                LocalDate.of(2022, 6, 21), "name");

        try {
            makePostRequest(user);
        } catch (Throwable ignored){}

        Collection<User> users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[]",
                users.toString());
    }

    @Test
    void shouldPostWithoutName() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "");

        try {
            makePostRequest(user);
        } catch (Throwable ignored){}

        Collection<User> users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=login}]",
                users.toString());
    }

    @Test
    void shouldNotPostWithBirthdayInFuture() {
        User user = new User("email@ru", "login",
                LocalDate.of(2122, 6, 21), "name");

        try {
            makePostRequest(user);
        } catch (Throwable ignored){}

        Collection<User> users =  restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[]",
                users.toString());
    }

    @Test
    void shouldPostAndNotPutWithEmptyEmail() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name");

        try {
            user = returnCreatedFilmAfterPostRequest(user);
        } catch (Throwable ignored){}

        user.setEmail("");

        try	{
            makePutRequest(user);
        } catch (Throwable ignored){}

        Collection<User> users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=name}]",
                users.toString());
    }

    @Test
    void shouldPostAndNotPutWithoutSymbolInEmail() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name");

        try {
            makePostRequest(user);
        } catch (Throwable ignored){}

        user.setEmail("email");
        try	{
            makePutRequest(user);
        } catch (Throwable ignored){}

        Collection<User> users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=name}]",
                users.toString());
    }

    @Test
    void shouldPostAndNotPutWithEmptyLogin() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name");

        try {
            user = returnCreatedFilmAfterPostRequest(user);
        } catch (Throwable ignored){}

        user.setLogin("");

        try	{
            makePutRequest(user);
        } catch (Throwable ignored){}

        Collection<User> users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=name}]",
                users.toString());
    }

    @Test
    void shouldPostAndNotPutWithSpaceInLogin() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name");

        try {
            makePostRequest(user);
        } catch (Throwable ignored){}

        user.setLogin("lo gin");

        try	{
            makePutRequest(user);
        } catch (Throwable ignored){}

        Collection<User> users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=name}]",
                users.toString());
    }

    @Test
    void shouldPostAndPutWithoutName() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name");

        try {
            user = returnCreatedFilmAfterPostRequest(user);
        } catch (Throwable ignored){}

        user.setName("");

        try	{
            makePutRequest(user);
        } catch (Throwable ignored){}

        Collection<User> users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=login}]",
                users.toString());
    }

    @Test
    void shouldPostAndNotPutWithBirthdayInFuture() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name");

        try {
            user = returnCreatedFilmAfterPostRequest(user);
        } catch (Throwable ignored){}

        user.setBirthday(LocalDate.of(2122, 6, 21));

        try	{
            makePutRequest(user);
        } catch (Throwable ignored){}

        Collection<User> users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=name}]",
                users.toString());
    }

    Gson makeGson() {
        GsonBuilder gsonBuilder  = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        gson = gsonBuilder.serializeNulls().setPrettyPrinting().create();
        return gson;
    }

    private void makePostRequest(User user) {
        HttpEntity<User> request = new HttpEntity<>(user);
        restTemplate.exchange(url,
                HttpMethod.POST,
                request,
                String.class);
    }

    private void makePutRequest(User user) {
        HttpEntity<User> newRequest = new HttpEntity<>(user);
        restTemplate.exchange(
                url,
                HttpMethod.PUT,
                newRequest,
                String.class);
    }

    private User returnCreatedFilmAfterPostRequest(User user) {
        HttpEntity<User> request = new HttpEntity<>(user);
        String productCreateResponse = restTemplate.postForObject(url, request, String.class);
        return gson.fromJson(productCreateResponse, User.class);
    }
}