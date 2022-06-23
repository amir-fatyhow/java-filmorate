package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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

	Gson makeGson() {
		GsonBuilder gsonBuilder  = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
		gson = gsonBuilder.serializeNulls().setPrettyPrinting().create();
		return gson;
	}

	@Test
	void postAndGetFilm() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
		Film film = new Film("name", "description",
				LocalDate.of(2022, 6, 21), 60);

		HttpEntity<Film> request = new HttpEntity<>(film);

		// POST request
		restTemplate.exchange(url,
				HttpMethod.POST,
				request,
				String.class);

		// GET request
		ResponseEntity<Collection> response	= restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-21, " +
						"duration=60}]", films.toString());

		ctx.close();
	}

	@Test
	void postPutAndGetFilm() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,21), 60);

		// POST request
		HttpEntity<Film> request = new HttpEntity<>(film);
		String productCreateResponse = restTemplate.postForObject(url, request, String.class);
		Film createdFilm = gson.fromJson(productCreateResponse, Film.class);
		createdFilm.setDescription("newDescription");
		createdFilm.setDuration(70);

		// PUT request
		HttpEntity<Film> newRequest = new HttpEntity<>(createdFilm);
		restTemplate.exchange(
				url,
				HttpMethod.PUT,
				newRequest,
				String.class);

		// GET request
		ResponseEntity<Collection> response	= restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[{id=1, name=name, description=newDescription, " +
						"releaseDate=2022-06-21, duration=70}]", films.toString());

		ctx.close();
	}

	@Test
	void shouldNotPostFilmWhenIsEmpty() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
		Film film = new Film("", "description",
				LocalDate.of(2022,6,21), 60);

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			restTemplate.exchange(url,
					HttpMethod.POST,
					request,
					String.class);
		} catch (Throwable ignored){}

		ResponseEntity<Collection> response = restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[]", films.toString());

		ctx.close();
	}

	@Test
	void shouldPostFilmWhenDescriptionEquals200() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
		Film film = new Film("name", "Sed ut perspiciatis unde omnis iste natus error sit voluptatem " +
				"accusantium doloremque laudantium," +
				" totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae " +
				"vitae dicta.",
				LocalDate.of(2022,6,21), 60);

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			restTemplate.exchange(url,
					HttpMethod.POST,
					request,
					String.class);
		} catch (Throwable ignored){}

		ResponseEntity<Collection> response	= restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[{id=1, name=name, description=Sed ut perspiciatis unde omnis iste natus " +
						"error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa " +
						"quae ab illo inventore veritatis et quasi architecto beatae vitae dicta., " +
						"releaseDate=2022-06-21, duration=60}]", films.toString());

		ctx.close();
	}

	@Test
	void shouldNotPostFilmWhenDescriptionMore200() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
		Film film = new Film("name", "Sed ut perspiciatis unde omnis iste natus error sit voluptatem " +
				"accusantium doloremque laudantium," +
				" totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae " +
				"vitae dicta.+",
				LocalDate.of(2022,6,21), 60);

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			restTemplate.exchange(url,
					HttpMethod.POST,
					request,
					String.class);
		} catch (Throwable ignored){}

		ResponseEntity<Collection> response	= restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[]", films.toString());

		ctx.close();
	}

	@Test
	void shouldNotPostFilmWhenReleaseDateIs1895_12_28() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
		Film film = new Film("name", "description",
				LocalDate.of(1895,12,28), 60);

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			restTemplate.exchange(url,
					HttpMethod.POST,
					request,
					String.class);
		} catch (Throwable ignored){}

		ResponseEntity<Collection> response	= restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=1895-12-28, " +
						"duration=60}]", films.toString());

		ctx.close();
	}

	@Test
	void shouldNotPostFilmWhenReleaseDateIsBefore1895_12_28() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
		Film film = new Film("name", "description",
				LocalDate.of(1800,6,22), 60);

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			restTemplate.exchange(url,
					HttpMethod.POST,
					request,
					String.class);
		} catch (Throwable ignored){}

		ResponseEntity<Collection> response	= restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[]", films.toString());

		ctx.close();
	}

	@Test
	void shouldPostFilmWhenReleaseDateIsAfter1895_12_28() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 60);

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			restTemplate.exchange(url,
					HttpMethod.POST,
					request,
					String.class);
		} catch (Throwable ignored){}

		ResponseEntity<Collection> response	= restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-22, " +
						"duration=60}]", films.toString());

		ctx.close();
	}

	@Test
	void shouldNotPostFilmWhenReleaseDurationIs0() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 0);

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			restTemplate.exchange(url,
					HttpMethod.POST,
					request,
					String.class);
		} catch (Throwable ignored){}

		ResponseEntity<Collection> response	= restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[]", films.toString());

		ctx.close();
	}

	@Test
	void shouldNotPostFilmWhenReleaseDurationIsLess0() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), -10);

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			restTemplate.exchange(url,
					HttpMethod.POST,
					request,
					String.class);
		} catch (Throwable ignored){}

		ResponseEntity<Collection> response	= restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[]", films.toString());

		ctx.close();
	}

	@Test
	void shouldNotPutFilmWhenIsEmpty() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,21), 60);

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			String productCreateResponse = restTemplate.postForObject(url, request, String.class);
			film = gson.fromJson(productCreateResponse, Film.class);
		} catch (Throwable ignored){}

		// PUT request
		film.setName("");
		film.setDescription("newDescription");
		film.setReleaseDate(LocalDate.of(2022,6,22));
		film.setDuration(70);

		try	{
			HttpEntity<Film> newRequest = new HttpEntity<>(film);
			restTemplate.exchange(
					url,
					HttpMethod.PUT,
					newRequest,
					String.class);
		} catch (Throwable ignored){}

		ResponseEntity<Collection> response	= restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-21, " +
						"duration=60}]", films.toString());

		ctx.close();
	}

	@Test
	void shouldPutFilmWhenDescriptionEquals200() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);

		// POST request
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 70);

		try	{
			HttpEntity<Film> request = new HttpEntity<>(film);
			String productCreateResponse = restTemplate.postForObject(url, request, String.class);
			film = gson.fromJson(productCreateResponse, Film.class);
		} catch (Throwable ignored){}

		// PUT request
		film.setDescription("Sed ut perspiciatis unde omnis iste natus error sit voluptatem " +
				"accusantium doloremque laudantium," +
				" totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae " +
				"vitae dicta.");
		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			restTemplate.exchange(url,
					HttpMethod.PUT,
					request,
					String.class);
		} catch (Throwable ignored){}

		ResponseEntity<Collection> response = restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[{id=1, name=name, description=Sed ut perspiciatis unde omnis iste natus " +
						"error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa " +
						"quae ab illo inventore veritatis et quasi architecto beatae vitae dicta., " +
						"releaseDate=2022-06-22, duration=70}]",
				films.toString());

		ctx.close();
	}

	@Test
	void shouldNotPutFilmWhenDescriptionMore200() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);

		// POST request
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 70);

		try	{
			HttpEntity<Film> request = new HttpEntity<>(film);
			String productCreateResponse = restTemplate.postForObject(url, request, String.class);
			film = gson.fromJson(productCreateResponse, Film.class);
		} catch (Throwable ignored){}

		// PUT request
		film.setDescription("Sed ut perspiciatis unde omnis iste natus error sit voluptatem " +
				"accusantium doloremque laudantium," +
				" totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae " +
				"vitae dicta.+");

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			restTemplate.exchange(url,
					HttpMethod.PUT,
					request,
					String.class);
		} catch (Throwable ignored){}

		ResponseEntity<Collection> response	= restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-22, " +
						"duration=70}]", films.toString());

		ctx.close();
	}

	@Test
	void shouldPutFilmWhenReleaseDateIs1895_12_28() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
		Film film = new Film("name", "description",
				LocalDate.of(2022,12,28), 60);

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			String productCreateResponse = restTemplate.postForObject(url, request, String.class);
			film = gson.fromJson(productCreateResponse, Film.class);
		} catch (Throwable ignored){}

		// PUT request
		film.setReleaseDate(LocalDate.of(1895,12,28));

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			restTemplate.exchange(url,
					HttpMethod.PUT,
					request,
					String.class);
		} catch (Throwable ignored){}

		ResponseEntity<Collection> response	= restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=1895-12-28, " +
						"duration=60}]", films.toString());

		ctx.close();
	}

	@Test
	void shouldNotPutFilmWhenReleaseDateIsBefore1895_12_28() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
		Film film = new Film("name", "description",
				LocalDate.of(2022,12,28), 60);

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			String productCreateResponse = restTemplate.postForObject(url, request, String.class);
			film = gson.fromJson(productCreateResponse, Film.class);
		} catch (Throwable ignored){}

		// PUT request
		film.setReleaseDate(LocalDate.of(1800,12,28));

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			restTemplate.exchange(url,
					HttpMethod.PUT,
					request,
					String.class);
		} catch (Throwable ignored){}

		ResponseEntity<Collection> response	= restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-12-28, " +
						"duration=60}]", films.toString());

		ctx.close();
	}

	@Test
	void shouldPutFilmWhenReleaseDateIsAfter1895_12_28() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
		Film film = new Film("name", "description",
				LocalDate.of(1895, 12, 28), 60);

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			String productCreateResponse = restTemplate.postForObject(url, request, String.class);
			film = gson.fromJson(productCreateResponse, Film.class);
		} catch (Throwable ignored) {
		}

		// PUT request
		film.setReleaseDate(LocalDate.of(2022, 12, 28));

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			restTemplate.exchange(url,
					HttpMethod.PUT,
					request,
					String.class);
		} catch (Throwable ignored) {}

		ResponseEntity<Collection> response	= restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-12-28, " +
						"duration=60}]", films.toString());

		ctx.close();
	}

	@Test
	void shouldNotPutFilmWhenReleaseDurationIs0() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 60);

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			String productCreateResponse = restTemplate.postForObject(url, request, String.class);
			film = gson.fromJson(productCreateResponse, Film.class);
		} catch (Throwable ignored){}

		// PUT request
		film.setDuration(0);

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			restTemplate.exchange(url,
					HttpMethod.PUT,
					request,
					String.class);
		} catch (Throwable ignored) {}

		ResponseEntity<Collection> response	= restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-22, " +
						"duration=60}]", films.toString());

		ctx.close();
	}

	@Test
	void shouldNotPutFilmWhenReleaseDurationIsLess0() {
		ConfigurableApplicationContext ctx = SpringApplication.run(FilmorateApplication.class);
		Film film = new Film("name", "description",
				LocalDate.of(2022,6,22), 60);

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			String productCreateResponse = restTemplate.postForObject(url, request, String.class);
			film = gson.fromJson(productCreateResponse, Film.class);
		} catch (Throwable ignored){}

		// PUT request
		film.setDuration(-10);

		try {
			HttpEntity<Film> request = new HttpEntity<>(film);
			restTemplate.exchange(url,
					HttpMethod.PUT,
					request,
					String.class);
		} catch (Throwable ignored) {}

		ResponseEntity<Collection> response	= restTemplate.getForEntity(url, Collection.class);
		Collection<Film> films = response.getBody();

		Assertions.assertEquals("[{id=1, name=name, description=description, releaseDate=2022-06-22, " +
						"duration=60}]", films.toString());

		ctx.close();
	}
}
