package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.adapter.LocalDateDeserializer;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@SpringBootTest
class FilmTest {
	private String url = "http://localhost:8080/films";
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
	@DisplayName("Should post film with correct data and get film")
	void postAndGetFilm() {
		Film film = new Film("name", "description",
				LocalDate.of(2022, 6, 21), 60);

		makePostRequest(film);

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-21, " +
				"duration=60, countLike=0, usersId=[]}]", films.toString());
	}

	@Test
	@DisplayName("Should post film with correct data then post with another description, duration and get film")
	void postPutAndGetFilm() {
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,21), 60);

		Film createdFilm = returnCreatedFilmAfterPostRequest(film);

		createdFilm.setDescription("newDescription");
		createdFilm.setDuration(70);

		makePutRequest(createdFilm);

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=newDescription, releaseDate=2022-06-21, " +
				"duration=70, countLike=0, usersId=[]}]", films.toString());
	}

	@Test
	void shouldNotPostFilmWhenIsEmpty() {
		Film film = new Film("", "description",
				LocalDate.of(2022,6,21), 60);

		try {
			makePostRequest(film);
		} catch (Throwable ignored){}

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[]", films.toString());
	}

	@Test
	void shouldPostFilmWhenDescriptionEquals200() {
		Film film = new Film("name", "Sed ut perspiciatis unde omnis iste natus error sit voluptatem " +
				"accusantium doloremque laudantium," +
				" totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae " +
				"vitae dicta.",
				LocalDate.of(2022,6,21), 60);

		try {
			makePostRequest(film);
		} catch (Throwable ignored){}

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=Sed ut perspiciatis unde omnis iste natus " +
				"error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo " +
				"inventore veritatis et quasi architecto beatae vitae dicta., releaseDate=2022-06-21, duration=60, " +
				"countLike=0, usersId=[]}]", films.toString());
	}

	@Test
	void shouldNotPostFilmWhenDescriptionMore200() {
		Film film = new Film("name", "Sed ut perspiciatis unde omnis iste natus error sit voluptatem " +
				"accusantium doloremque laudantium," +
				" totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae " +
				"vitae dicta.+",
				LocalDate.of(2022,6,21), 60);

		try {
			makePostRequest(film);
		} catch (Throwable ignored){}

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[]", films.toString());
	}

	@Test
	void shouldNotPostFilmWhenReleaseDateIs1895_12_28() {
		Film film = new Film("name", "description",
				LocalDate.of(1895,12,28), 60);

		try {
			makePostRequest(film);
		} catch (Throwable ignored){}

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[]", films.toString());
	}

	@Test
	void shouldNotPostFilmWhenReleaseDateIsBefore1895_12_28() {
		Film film = new Film("name", "description",
				LocalDate.of(1800,6,22), 60);

		try {
			makePostRequest(film);
		} catch (Throwable ignored){}

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[]", films.toString());
	}

	@Test
	void shouldPostFilmWhenReleaseDateIsAfter1895_12_28() {
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 60);

		try {
			makePostRequest(film);
		} catch (Throwable ignored){}

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-22," +
				" duration=60, countLike=0, usersId=[]}]", films.toString());
	}

	@Test
	void shouldNotPostFilmWhenReleaseDurationIs0() {
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 0);

		try {
			makePostRequest(film);
		} catch (Throwable ignored){}

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[]", films.toString());
	}

	@Test
	void shouldNotPostFilmWhenReleaseDurationIsLess0() {
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), -10);

		try {
			makePostRequest(film);
		} catch (Throwable ignored){}

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[]", films.toString());
	}

	@Test
	void shouldNotPutFilmWhenIsEmpty() {
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,21), 60);

		try {
			film = returnCreatedFilmAfterPostRequest(film);
		} catch (Throwable ignored){}

		film.setName("");
		film.setDescription("newDescription");
		film.setReleaseDate(LocalDate.of(2022,6,22));
		film.setDuration(70);

		try	{
			makePutRequest(film);
		} catch (Throwable ignored){}

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-21, " +
				"duration=60, countLike=0, usersId=[]}]", films.toString());
	}

	@Test
	void shouldPutFilmWhenDescriptionEquals200() {
		String descriptionWith200Symbols = "Sed ut perspiciatis unde omnis iste natus error sit voluptatem " +
				"accusantium doloremque laudantium," +
				" totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae " +
				"vitae dicta.";

		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 70);

		try	{
			film = returnCreatedFilmAfterPostRequest(film);
		} catch (Throwable ignored){}

		film.setDescription(descriptionWith200Symbols);
		try {
			makePutRequest(film);
		} catch (Throwable ignored){}

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=Sed ut perspiciatis unde omnis iste natus " +
						"error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa " +
						"quae ab illo inventore veritatis et quasi architecto beatae vitae dicta., " +
						"releaseDate=2022-06-22, duration=70, countLike=0, usersId=[]}]",
				films.toString());
	}

	@Test
	void shouldNotPutFilmWhenDescriptionMore200() {
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 70);

		try	{
			film = returnCreatedFilmAfterPostRequest(film);
		} catch (Throwable ignored){}

		film.setDescription("Sed ut perspiciatis unde omnis iste natus error sit voluptatem " +
				"accusantium doloremque laudantium," +
				" totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae " +
				"vitae dicta.+");

		try {
			makePostRequest(film);
		} catch (Throwable ignored){}

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-22, " +
				"duration=70, countLike=0, usersId=[]}]", films.toString());
	}

	@Test
	void shouldNotPutFilmWhenReleaseDateIs1895_12_28() {
		Film film = new Film("name", "description",
				LocalDate.of(2022,12,28), 60);

		try {
			film = returnCreatedFilmAfterPostRequest(film);
		} catch (Throwable ignored){}

		film.setReleaseDate(LocalDate.of(1895,12,28));

		try {
			makePutRequest(film);
		} catch (Throwable ignored){}

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-12-28, " +
				"duration=60, countLike=0, usersId=[]}]", films.toString());
	}

	@Test
	void shouldNotPutFilmWhenReleaseDateIsBefore1895_12_28() {
		Film film = new Film("name", "description",
				LocalDate.of(2022,12,28), 60);

		try {
			film = returnCreatedFilmAfterPostRequest(film);
		} catch (Throwable ignored){}

		film.setReleaseDate(LocalDate.of(1800,12,28));

		try {
			makePutRequest(film);
		} catch (Throwable ignored){}

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-12-28, " +
				"duration=60, countLike=0, usersId=[]}]", films.toString());
	}

	@Test
	void shouldPutFilmWhenReleaseDateIsAfter1895_12_28() {
		Film film = new Film("name", "description",
				LocalDate.of(1900, 12, 28), 60);

		try {
			film = returnCreatedFilmAfterPostRequest(film);;
		} catch (Throwable ignored) {}

		film.setReleaseDate(LocalDate.of(2022, 12, 28));

		try {
			makePutRequest(film);
		} catch (Throwable ignored) {}

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-12-28, " +
				"duration=60, countLike=0, usersId=[]}]", films.toString());
	}

	@Test
	void shouldNotPutFilmWhenReleaseDurationIs0() {
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 60);

		try {
			film = returnCreatedFilmAfterPostRequest(film);
		} catch (Throwable ignored){}

		film.setDuration(0);

		try {
			makePutRequest(film);
		} catch (Throwable ignored) {}

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-22, " +
				"duration=60, countLike=0, usersId=[]}]", films.toString());
	}

	@Test
	void shouldNotPutFilmWhenReleaseDurationIsLess0() {
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 60);

		try {
			film = returnCreatedFilmAfterPostRequest(film);
		} catch (Throwable ignored){}

		film.setDuration(-10);

		try {
			makePutRequest(film);
		} catch (Throwable ignored) {}

		Collection films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-22, " +
				"duration=60, countLike=0, usersId=[]}]", films.toString());
	}

	@Test
	void shouldGetFilmById() {
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 60);
		String urlFilmId1 = "http://localhost:8080/films/1";

		makePostRequest(film);

		Film filmById = restTemplate.getForEntity(urlFilmId1, Film.class).getBody();

		Assertions.assertEquals("Film(id=1, name=name, description=description, releaseDate=2022-06-22, " +
				"duration=60, countLike=0, usersId=[])", filmById.toString());
	}

	@Test
	void shouldNotGetFilmByIncorrectId() {
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 60);
		String urlFilmId1 = "http://localhost:8080/films/2";

		makePostRequest(film);

		boolean isGetFilmByIncorrectId = true;
		try {
			restTemplate.getForEntity(urlFilmId1, Film.class).getBody();
		} catch (Throwable ex){
			isGetFilmByIncorrectId = false;
		}

		Assertions.assertFalse(isGetFilmByIncorrectId);
	}

	@Test
	void shouldPutLike() {
		User user = new User("email@ru", "login",
				LocalDate.of(2022, 6, 21), "name");

		makePostRequestUser(user);

		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 60);

		makePostRequest(film);
		putLike(user, URI.create("http://localhost:8080/films/1/like/1"));

		String urlFilmId1 = "http://localhost:8080/films/1";
		Film filmById = restTemplate.getForEntity(urlFilmId1, Film.class).getBody();

		Assertions.assertEquals("Film(id=1, name=name, description=description, releaseDate=2022-06-22, " +
				"duration=60, countLike=1, usersId=[1])", filmById.toString());
	}

	@Test
	void shouldNotPutTwoLike() {
		User user = new User("email@ru", "login",
				LocalDate.of(2022, 6, 21), "name");

		makePostRequestUser(user);

		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 60);

		makePostRequest(film);
		putLike(user, URI.create("http://localhost:8080/films/1/like/1"));
		putLike(user, URI.create("http://localhost:8080/films/1/like/1"));

		String urlFilmId1 = "http://localhost:8080/films/1";
		Film filmById = restTemplate.getForEntity(urlFilmId1, Film.class).getBody();

		Assertions.assertEquals("Film(id=1, name=name, description=description, releaseDate=2022-06-22, " +
				"duration=60, countLike=1, usersId=[1])", filmById.toString());
	}

	@Test
	void shouldDeleteLike() {
		User user = new User("email@ru", "login",
				LocalDate.of(2022, 6, 21), "name");

		makePostRequestUser(user);

		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 60);

		makePostRequest(film);
		putLike(user, URI.create("http://localhost:8080/films/1/like/1"));
		deleteLike(user, URI.create("http://localhost:8080/films/1/like/1"));

		String urlFilmId1 = "http://localhost:8080/films/1";
		Film filmById = restTemplate.getForEntity(urlFilmId1, Film.class).getBody();

		Assertions.assertEquals("Film(id=1, name=name, description=description, releaseDate=2022-06-22, " +
				"duration=60, countLike=0, usersId=[])", filmById.toString());
	}

	@Test
	void shouldGetPopularFilmsWithoutCount() {
		createTenFilms();
		createUsersAndPutLikes();

		String url = "http://localhost:8080/films/popular";
		List<Film> films = restTemplate.getForEntity(url, List.class).getBody();

		Assertions.assertEquals("[{id=10, name=film10, description=description, releaseDate=2010-10-10, " +
				"duration=10, countLike=10, usersId=[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]}, {id=9, name=film9, " +
				"description=description, releaseDate=2009-09-09, duration=9, countLike=9, " +
				"usersId=[1, 2, 3, 4, 5, 6, 7, 8, 9]}, {id=8, name=film8, description=description, " +
				"releaseDate=2008-08-08, duration=8, countLike=8, usersId=[1, 2, 3, 4, 5, 6, 7, 8]}, " +
				"{id=7, name=film7, description=description, releaseDate=2007-07-07, duration=7, countLike=7, " +
				"usersId=[1, 2, 3, 4, 5, 6, 7]}, {id=6, name=film6, description=description, releaseDate=2006-06-06, " +
				"duration=6, countLike=6, usersId=[1, 2, 3, 4, 5, 6]}, {id=5, name=film5, description=description, " +
				"releaseDate=2005-05-05, duration=5, countLike=5, usersId=[1, 2, 3, 4, 5]}, {id=4, name=film4, " +
				"description=description, releaseDate=2004-04-04, duration=4, countLike=4, usersId=[1, 2, 3, 4]}, " +
				"{id=3, name=film3, description=description, releaseDate=2003-03-03, duration=3, countLike=3, " +
				"usersId=[1, 2, 3]}, {id=2, name=film2, description=description, releaseDate=2002-02-02, " +
				"duration=2, countLike=2, usersId=[1, 2]}, {id=1, name=film1, description=description, " +
				"releaseDate=2001-01-01, duration=1, countLike=1, usersId=[1]}]", films.toString());
	}

	@Test
	void shouldGetPopularFilmsWithCountEquals2() {
		createTenFilms();
		createUsersAndPutLikes();

		String url = "http://localhost:8080/films/popular?count=2";
		List<Film> films = restTemplate.getForEntity(url, List.class).getBody();

		Assertions.assertEquals("[{id=10, name=film10, description=description, releaseDate=2010-10-10, " +
				"duration=10, countLike=10, usersId=[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]}, {id=9, name=film9, " +
				"description=description, releaseDate=2009-09-09, duration=9, countLike=9, " +
				"usersId=[1, 2, 3, 4, 5, 6, 7, 8, 9]}]", films.toString());
	}

	@Test
	void shouldGetPopularFilmsWithIncorrectCount() {
		createTenFilms();
		createUsersAndPutLikes();

		String url = "http://localhost:8080/films/popular?count=-1";

		boolean isGetFilmByIncorrectId = true;
		try {
			restTemplate.getForEntity(url, List.class).getBody();
		} catch (Throwable ex){
			isGetFilmByIncorrectId = false;
		}

		Assertions.assertFalse(isGetFilmByIncorrectId);
	}

	private void createTenFilms() {
		Film film1 = new Film("film1", "description",
				LocalDate.of(2001,1,1), 1);
		makePostRequest(film1);

		Film film2 = new Film("film2", "description",
				LocalDate.of(2002,2,2), 2);
		makePostRequest(film2);

		Film film3 = new Film("film3", "description",
				LocalDate.of(2003,3,3), 3);
		makePostRequest(film3);

		Film film4 = new Film("film4", "description",
				LocalDate.of(2004,4,4), 4);
		makePostRequest(film4);

		Film film5 = new Film("film5", "description",
				LocalDate.of(2005,5,5), 5);
		makePostRequest(film5);

		Film film6 = new Film("film6", "description",
				LocalDate.of(2006,6,6), 6);
		makePostRequest(film6);

		Film film7 = new Film("film7", "description",
				LocalDate.of(2007,7,7), 7);
		makePostRequest(film7);

		Film film8 = new Film("film8", "description",
				LocalDate.of(2008,8,8), 8);
		makePostRequest(film8);

		Film film9 = new Film("film9", "description",
				LocalDate.of(2009,9,9), 9);
		makePostRequest(film9);

		Film film10 = new Film("film10", "description",
				LocalDate.of(2010,10,10), 10);
		makePostRequest(film10);
	}

	private void createUsersAndPutLikes() {
		User user1 = new User("email@ru", "login",
				LocalDate.of(2001, 1, 1), "name");
		makePostRequestUser(user1);

		User user2 = new User("email@ru", "login",
				LocalDate.of(2002, 2, 2), "name");
		makePostRequestUser(user2);

		User user3 = new User("email@ru", "login",
				LocalDate.of(2003, 3, 3), "name");
		makePostRequestUser(user3);

		User user4 = new User("email@ru", "login",
				LocalDate.of(2004, 4, 4), "name");
		makePostRequestUser(user4);

		User user5 = new User("email@ru", "login",
				LocalDate.of(2005, 5, 5), "name");
		makePostRequestUser(user5);

		User user6 = new User("email@ru", "login",
				LocalDate.of(2006, 6, 6), "name");
		makePostRequestUser(user6);

		User user7 = new User("email@ru", "login",
				LocalDate.of(2007, 7, 7), "name");
		makePostRequestUser(user7);

		User user8 = new User("email@ru", "login",
				LocalDate.of(2008, 8, 8), "name");
		makePostRequestUser(user8);

		User user9 = new User("email@ru", "login",
				LocalDate.of(2009, 9, 9), "name");
		makePostRequestUser(user4);

		User user10 = new User("email@ru", "login",
				LocalDate.of(2010, 10, 10), "name");
		makePostRequestUser(user10);

		putLike(user1, URI.create("http://localhost:8080/films/1/like/1"));

		putLike(user1, URI.create("http://localhost:8080/films/2/like/1"));
		putLike(user2, URI.create("http://localhost:8080/films/2/like/2"));

		putLike(user1, URI.create("http://localhost:8080/films/3/like/1"));
		putLike(user2, URI.create("http://localhost:8080/films/3/like/2"));
		putLike(user3, URI.create("http://localhost:8080/films/3/like/3"));

		putLike(user1, URI.create("http://localhost:8080/films/4/like/1"));
		putLike(user2, URI.create("http://localhost:8080/films/4/like/2"));
		putLike(user3, URI.create("http://localhost:8080/films/4/like/3"));
		putLike(user4, URI.create("http://localhost:8080/films/4/like/4"));

		putLike(user1, URI.create("http://localhost:8080/films/5/like/1"));
		putLike(user2, URI.create("http://localhost:8080/films/5/like/2"));
		putLike(user3, URI.create("http://localhost:8080/films/5/like/3"));
		putLike(user4, URI.create("http://localhost:8080/films/5/like/4"));
		putLike(user5, URI.create("http://localhost:8080/films/5/like/5"));

		putLike(user1, URI.create("http://localhost:8080/films/6/like/1"));
		putLike(user2, URI.create("http://localhost:8080/films/6/like/2"));
		putLike(user3, URI.create("http://localhost:8080/films/6/like/3"));
		putLike(user4, URI.create("http://localhost:8080/films/6/like/4"));
		putLike(user5, URI.create("http://localhost:8080/films/6/like/5"));
		putLike(user6, URI.create("http://localhost:8080/films/6/like/6"));

		putLike(user1, URI.create("http://localhost:8080/films/7/like/1"));
		putLike(user2, URI.create("http://localhost:8080/films/7/like/2"));
		putLike(user3, URI.create("http://localhost:8080/films/7/like/3"));
		putLike(user4, URI.create("http://localhost:8080/films/7/like/4"));
		putLike(user5, URI.create("http://localhost:8080/films/7/like/5"));
		putLike(user6, URI.create("http://localhost:8080/films/7/like/6"));
		putLike(user7, URI.create("http://localhost:8080/films/7/like/7"));

		putLike(user1, URI.create("http://localhost:8080/films/8/like/1"));
		putLike(user2, URI.create("http://localhost:8080/films/8/like/2"));
		putLike(user3, URI.create("http://localhost:8080/films/8/like/3"));
		putLike(user4, URI.create("http://localhost:8080/films/8/like/4"));
		putLike(user5, URI.create("http://localhost:8080/films/8/like/5"));
		putLike(user6, URI.create("http://localhost:8080/films/8/like/6"));
		putLike(user7, URI.create("http://localhost:8080/films/8/like/7"));
		putLike(user8, URI.create("http://localhost:8080/films/8/like/8"));

		putLike(user1, URI.create("http://localhost:8080/films/9/like/1"));
		putLike(user2, URI.create("http://localhost:8080/films/9/like/2"));
		putLike(user3, URI.create("http://localhost:8080/films/9/like/3"));
		putLike(user4, URI.create("http://localhost:8080/films/9/like/4"));
		putLike(user5, URI.create("http://localhost:8080/films/9/like/5"));
		putLike(user6, URI.create("http://localhost:8080/films/9/like/6"));
		putLike(user7, URI.create("http://localhost:8080/films/9/like/7"));
		putLike(user8, URI.create("http://localhost:8080/films/9/like/8"));
		putLike(user9, URI.create("http://localhost:8080/films/9/like/9"));

		putLike(user1, URI.create("http://localhost:8080/films/10/like/1"));
		putLike(user2, URI.create("http://localhost:8080/films/10/like/2"));
		putLike(user3, URI.create("http://localhost:8080/films/10/like/3"));
		putLike(user4, URI.create("http://localhost:8080/films/10/like/4"));
		putLike(user5, URI.create("http://localhost:8080/films/10/like/5"));
		putLike(user6, URI.create("http://localhost:8080/films/10/like/6"));
		putLike(user7, URI.create("http://localhost:8080/films/10/like/7"));
		putLike(user8, URI.create("http://localhost:8080/films/10/like/8"));
		putLike(user9, URI.create("http://localhost:8080/films/10/like/9"));
		putLike(user10, URI.create("http://localhost:8080/films/10/like/10"));
	}

	private void deleteLike(User user, URI url) {
		HttpEntity<User> newRequest = new HttpEntity<>(user);
		restTemplate.exchange(
				url,
				HttpMethod.DELETE,
				newRequest,
				String.class);
	}

	private void putLike(User user, URI url) {
		HttpEntity<User> newRequest = new HttpEntity<>(user);
		restTemplate.exchange(
				url,
				HttpMethod.PUT,
				newRequest,
				String.class);
	}

	private void makePostRequestUser(User user) {
		HttpEntity<User> request = new HttpEntity<>(user);
		restTemplate.exchange("http://localhost:8080/users",
				HttpMethod.POST,
				request,
				String.class);
	}


	private Gson makeGson() {
		GsonBuilder gsonBuilder  = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
		gson = gsonBuilder.serializeNulls().setPrettyPrinting().create();
		return gson;
	}

	private void makePostRequest(Film film) {
		HttpEntity<Film> request = new HttpEntity<>(film);
		restTemplate.exchange(url,
				HttpMethod.POST,
				request,
				String.class);
	}

	private void makePutRequest(Film createdFilm) {
		HttpEntity<Film> newRequest = new HttpEntity<>(createdFilm);
		restTemplate.exchange(
				url,
				HttpMethod.PUT,
				newRequest,
				String.class);
	}

	private Film returnCreatedFilmAfterPostRequest(Film film) {
		HttpEntity<Film> request = new HttpEntity<>(film);
		String productCreateResponse = restTemplate.postForObject(url, request, String.class);
		return gson.fromJson(productCreateResponse, Film.class);
	}
}
