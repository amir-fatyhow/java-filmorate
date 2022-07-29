package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.adapter.LocalDateDeserializer;
import ru.yandex.practicum.filmorate.model.User;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
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
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        makePostRequest(user);

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, " +
                        "name=name, friends={}}]",
                users.toString());
    }

    @Test
    @DisplayName("Should post user with correct data then post with another login, name and get user")
    void postPutAndGetFilm() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        User createdUser = returnCreatedUserAfterPostRequest(user);
        createdUser.setLogin("updatedLogin");
        createdUser.setName("updatedName");

        makePutRequest(createdUser);

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=updatedLogin, birthday=2022-06-21, " +
                        "name=updatedName, friends={}}]",
                users.toString());
    }

    @Test
    void shouldNotPostWithEmptyEmail() {
        User user = new User("", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        try {
            makePostRequest(user);
        } catch (Throwable ignored){}

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[]",
                users.toString());
    }

    @Test
    void shouldNotPostWithoutSymbolInEmail() {
        User user = new User("emailru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        try {
            makePostRequest(user);
        } catch (Throwable ignored){}

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[]",
                users.toString());
    }

    @Test
    void shouldNotPostWithEmptyLogin() {
        User user = new User("email@ru", "",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        try {
            makePostRequest(user);
        } catch (Throwable ignored){}

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[]",
                users.toString());
    }

    @Test
    void shouldNotPostWithSpaceInLogin() {
        User user = new User("email@ru", "lo gin",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        try {
            makePostRequest(user);
        } catch (Throwable ignored){}

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[]",
                users.toString());
    }

    @Test
    void shouldPostWithoutName() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "", new HashMap<>());

        try {
            makePostRequest(user);
        } catch (Throwable ignored){}

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, " +
                        "name=login, friends={}}]",
                users.toString());
    }

    @Test
    void shouldNotPostWithBirthdayInFuture() {
        User user = new User("email@ru", "login",
                LocalDate.of(2122, 6, 21), "name", new HashMap<>());

        try {
            makePostRequest(user);
        } catch (Throwable ignored){}

        Collection users =  restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[]",
                users.toString());
    }

    @Test
    void shouldPostAndNotPutWithEmptyEmail() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        try {
            user = returnCreatedUserAfterPostRequest(user);
        } catch (Throwable ignored){}

        user.setEmail("");

        try	{
            makePutRequest(user);
        } catch (Throwable ignored){}

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, " +
                        "name=name, friends={}}]",
                users.toString());
    }

    @Test
    void shouldPostAndNotPutWithoutSymbolInEmail() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        try {
            makePostRequest(user);
        } catch (Throwable ignored){}

        user.setEmail("email");
        try	{
            makePutRequest(user);
        } catch (Throwable ignored){}

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, " +
                        "name=name, friends={}}]",
                users.toString());
    }

    @Test
    void shouldPostAndNotPutWithEmptyLogin() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        try {
            user = returnCreatedUserAfterPostRequest(user);
        } catch (Throwable ignored){}

        user.setLogin("");

        try	{
            makePutRequest(user);
        } catch (Throwable ignored){}

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, " +
                        "name=name, friends={}}]",
                users.toString());
    }

    @Test
    void shouldPostAndNotPutWithSpaceInLogin() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        try {
            makePostRequest(user);
        } catch (Throwable ignored){}

        user.setLogin("lo gin");

        try	{
            makePutRequest(user);
        } catch (Throwable ignored){}

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21," +
                        " name=name, friends={}}]",
                users.toString());
    }

    @Test
    void shouldPostAndPutWithoutName() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        try {
            user = returnCreatedUserAfterPostRequest(user);
        } catch (Throwable ignored){}

        user.setName("");

        try	{
            makePutRequest(user);
        } catch (Throwable ignored){}

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, " +
                        "name=login, friends={}}]",
                users.toString());
    }

    @Test
    void shouldPostAndNotPutWithBirthdayInFuture() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        try {
            user = returnCreatedUserAfterPostRequest(user);
        } catch (Throwable ignored){}

        user.setBirthday(LocalDate.of(2122, 6, 21));

        try	{
            makePutRequest(user);
        } catch (Throwable ignored){}

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, " +
                        "name=name, friends={}}]",
                users.toString());
    }

    @Test
    void shouldGetUserById() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());
        String urlFilmId1 = "http://localhost:8080/users/1";

        makePostRequest(user);

        User userById = restTemplate.getForEntity(urlFilmId1, User.class).getBody();

        Assertions.assertEquals("User(id=1, email=email@ru, login=login, birthday=2022-06-21, " +
                        "name=name, friends={})",
                userById.toString());
    }

    @Test
    void shouldNotGetUserByIncorrectId() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());
        String urlFilmId1 = "http://localhost:8080/users/2";

        makePostRequest(user);

        boolean isGetUserByIncorrectId = true;
        try {
            restTemplate.getForEntity(urlFilmId1, User.class).getBody();
        } catch (Throwable ex){
            isGetUserByIncorrectId = false;
        }

        Assertions.assertFalse(isGetUserByIncorrectId);
    }

    @Test
    void shouldPutFriendWithoutConfirm() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        makePostRequest(user);

        User friend = new User("friend@ru", "friend",
                LocalDate.of(2021, 1, 11), "friend", new HashMap<>());

        makePostRequest(friend);
        makePutRequestToAddFriend(friend, URI.create("http://localhost:8080/users/1/friends/2"));

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, " +
                        "name=name, friends={2=true}}, " +
                        "{id=2, email=friend@ru, login=friend, birthday=2021-01-11, name=friend, friends={1=false}}]",
                users.toString());
    }

    @Test
    void shouldPutFriendWithConfirm() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        makePostRequest(user);

        User friend = new User("friend@ru", "friend",
                LocalDate.of(2021, 1, 11), "friend", new HashMap<>());

        makePostRequest(friend);
        makePutRequestToAddFriend(friend, URI.create("http://localhost:8080/users/1/friends/2"));

        String urlConfirm = "http://localhost:8080/users/1/confirm/2";
        HttpEntity<User> newRequest = new HttpEntity<>(user);
        restTemplate.exchange(
                urlConfirm,
                HttpMethod.PUT,
                newRequest,
                String.class);

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, " +
                        "name=name, friends={2=true}}, " +
                        "{id=2, email=friend@ru, login=friend, birthday=2021-01-11, name=friend, friends={1=true}}]",
                users.toString());
    }

    @Test
    void shouldNotPutFriendByIncorrectId() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        makePostRequest(user);

        User friend = new User("friend@ru", "friend",
                LocalDate.of(2021, 1, 11), "friend", new HashMap<>());

        makePostRequest(friend);

        try {
            makePutRequestToAddFriend(friend, URI.create("http://localhost:8080/users/1/friends/-1"));
        } catch (Throwable ignored){}


        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=name, " +
                        "friends={}}, {id=2, email=friend@ru, login=friend, birthday=2021-01-11, " +
                        "name=friend, friends={}}]",
                users.toString());
    }

    @Test
    void shouldDeleteFriend() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        makePostRequest(user);

        User friend = new User("friend@ru", "friend",
                LocalDate.of(2021, 1, 11), "friend", new HashMap<>());

        makePostRequest(friend);
        deleteFriend(user, URI.create("http://localhost:8080/users/1/friends/2"));

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=name, " +
                        "friends={}}, {id=2, email=friend@ru, login=friend, birthday=2021-01-11, " +
                        "name=friend, friends={}}]",
                users.toString());
    }

    @Test
    void shouldNotDeleteFriendByIncorrectId() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        makePostRequest(user);

        User friend = new User("friend@ru", "friend",
                LocalDate.of(2021, 1, 11), "friend", new HashMap<>());

        makePostRequest(friend);
        makePutRequestToAddFriend(friend, URI.create("http://localhost:8080/users/1/friends/2"));

        try {
            deleteFriend(user, URI.create("http://localhost:8080/users/1/friends/-1"));
        } catch (Throwable ignored){}

        Collection users = restTemplate.getForEntity(url, Collection.class).getBody();

        Assertions.assertEquals("[{id=1, email=email@ru, login=login, birthday=2022-06-21, name=name, " +
                            "friends={2=true}}, " +
                        "{id=2, email=friend@ru, login=friend, birthday=2021-01-11, name=friend, friends={1=false}}]",
                users.toString());
    }

    @Test
    void shouldGetFriends() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        makePostRequest(user);

        User friendOne = new User("friendOne@ru", "friendOne",
                LocalDate.of(2021, 1, 11), "friendOne", new HashMap<>());

        makePostRequest(friendOne);

        User friendTwo = new User("friendTwo@ru", "friendTwo",
                LocalDate.of(2001, 2, 11), "friendTwo", new HashMap<>());

        makePostRequest(friendTwo);

        makePutRequestToAddFriend(friendOne, URI.create("http://localhost:8080/users/1/friends/2"));
        makePutRequestToAddFriend(friendTwo, URI.create("http://localhost:8080/users/1/friends/3"));

        String urlUser = "http://localhost:8080/users/1/friends";

        List users = restTemplate.getForEntity(urlUser, List.class).getBody();

        Assertions.assertEquals("[{id=2, email=friendOne@ru, login=friendOne, birthday=2021-01-11, " +
                            "name=friendOne, friends={1=false}}, " +
                        "{id=3, email=friendTwo@ru, login=friendTwo, birthday=2001-02-11, " +
                        "name=friendTwo, friends={1=false}}]", users.toString());
    }

    @Test
    void shouldNotGetFriendsByIncorrectId() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        makePostRequest(user);

        User friendOne = new User("friendOne@ru", "friendOne",
                LocalDate.of(2021, 1, 11), "friendOne", new HashMap<>());

        makePostRequest(friendOne);

        User friendTwo = new User("friendTwo@ru", "friendTwo",
                LocalDate.of(2001, 2, 11), "friendTwo", new HashMap<>());

        makePostRequest(friendTwo);

        makePutRequestToAddFriend(friendOne, URI.create("http://localhost:8080/users/1/friends/2"));
        makePutRequestToAddFriend(friendTwo, URI.create("http://localhost:8080/users/1/friends/3"));

        String urlUser = "http://localhost:8080/users/-1/friends";

        boolean isGetFriend = true;
        try {
            restTemplate.getForEntity(urlUser, List.class).getBody();
        } catch (Throwable ex){
            isGetFriend = false;
        }

        Assertions.assertFalse(isGetFriend);
    }

    @Test
    void shouldGetCommonFriends() {
        User user = new User("email@ru", "login",
                LocalDate.of(2022, 6, 21), "name", new HashMap<>());

        makePostRequest(user);

        User friendOne = new User("friendOne@ru", "friendOne",
                LocalDate.of(2021, 1, 11), "friendOne", new HashMap<>());

        makePostRequest(friendOne);

        User friendTwo = new User("friendTwo@ru", "friendTwo",
                LocalDate.of(2001, 2, 11), "friendTwo", new HashMap<>());

        makePostRequest(friendTwo);

        makePutRequestToAddFriend(friendOne, URI.create("http://localhost:8080/users/1/friends/2"));
        makePutRequestToAddFriend(friendTwo, URI.create("http://localhost:8080/users/1/friends/3"));
        makePutRequestToAddFriend(friendTwo, URI.create("http://localhost:8080/users/2/friends/3"));

        String urlConfirm1 = "http://localhost:8080/users/1/confirm/2";
        String urlConfirm2 = "http://localhost:8080/users/1/confirm/3";
        String urlConfirm3 = "http://localhost:8080/users/2/confirm/3";
        HttpEntity<User> newRequest = new HttpEntity<>(user);
        restTemplate.exchange(urlConfirm1, HttpMethod.PUT, newRequest, String.class);
        restTemplate.exchange(urlConfirm2, HttpMethod.PUT, newRequest, String.class);
        restTemplate.exchange(urlConfirm3, HttpMethod.PUT, newRequest, String.class);

        String urlUser = "http://localhost:8080/users/1/friends/common/2";

        List commonFriend = restTemplate.getForEntity(urlUser, List.class).getBody();

        Assertions.assertEquals("[{id=3, email=friendTwo@ru, login=friendTwo, birthday=2001-02-11," +
                        " name=friendTwo, friends={1=true, 2=true}}]",
                commonFriend.toString());
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

    private void makePutRequestToAddFriend(User user, URI url) {
        HttpEntity<User> newRequest = new HttpEntity<>(user);
        restTemplate.exchange(
                url,
                HttpMethod.PUT,
                newRequest,
                String.class);
    }

    private void deleteFriend(User user, URI url) {
        HttpEntity<User> newRequest = new HttpEntity<>(user);
        restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                newRequest,
                String.class);
    }

    private User returnCreatedUserAfterPostRequest(User user) {
        HttpEntity<User> request = new HttpEntity<>(user);
        String productCreateResponse = restTemplate.postForObject(url, request, String.class);
        return gson.fromJson(productCreateResponse, User.class);
    }
}
