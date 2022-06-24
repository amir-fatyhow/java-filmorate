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

import java.time.LocalDate;
import java.util.Collection;


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

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-21, " +
						"duration=60}]", films.toString());
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

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=newDescription, " +
						"releaseDate=2022-06-21, duration=70}]", films.toString());
	}

	@Test
	void shouldNotPostFilmWhenIsEmpty() {
		Film film = new Film("", "description",
				LocalDate.of(2022,6,21), 60);

		try {
			makePostRequest(film);
		} catch (Throwable ignored){}

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

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

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=Sed ut perspiciatis unde omnis iste natus " +
						"error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa " +
						"quae ab illo inventore veritatis et quasi architecto beatae vitae dicta., " +
						"releaseDate=2022-06-21, duration=60}]", films.toString());
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

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[]", films.toString());
	}

	@Test
	void shouldNotPostFilmWhenReleaseDateIs1895_12_28() {
		Film film = new Film("name", "description",
				LocalDate.of(1895,12,28), 60);

		try {
			makePostRequest(film);
		} catch (Throwable ignored){}

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[]", films.toString());
	}

	@Test
	void shouldNotPostFilmWhenReleaseDateIsBefore1895_12_28() {
		Film film = new Film("name", "description",
				LocalDate.of(1800,6,22), 60);

		try {
			makePostRequest(film);
		} catch (Throwable ignored){}

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[]", films.toString());
	}

	@Test
	void shouldPostFilmWhenReleaseDateIsAfter1895_12_28() {
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 60);

		try {
			makePostRequest(film);
		} catch (Throwable ignored){}

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-22, " +
						"duration=60}]", films.toString());
	}

	@Test
	void shouldNotPostFilmWhenReleaseDurationIs0() {
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 0);

		try {
			makePostRequest(film);
		} catch (Throwable ignored){}

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[]", films.toString());
	}

	@Test
	void shouldNotPostFilmWhenReleaseDurationIsLess0() {
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), -10);

		try {
			makePostRequest(film);
		} catch (Throwable ignored){}

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

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

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-21, " +
						"duration=60}]", films.toString());
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

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=Sed ut perspiciatis unde omnis iste natus " +
						"error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa " +
						"quae ab illo inventore veritatis et quasi architecto beatae vitae dicta., " +
						"releaseDate=2022-06-22, duration=70}]",
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

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-22, " +
						"duration=70}]", films.toString());
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

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-12-28, " +
				"duration=60}]", films.toString());
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

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-12-28, " +
						"duration=60}]", films.toString());
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

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-12-28, " +
						"duration=60}]", films.toString());
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

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-22, " +
						"duration=60}]", films.toString());
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

		Collection<Film> films = restTemplate.getForEntity(url, Collection.class).getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-22, " +
						"duration=60}]", films.toString());
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
