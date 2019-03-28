package com.tom.estudy.vertx;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tom.estudy.vertx.model.Whisky;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class MyFirstVerticle extends AbstractVerticle {

	JDBCClient jdbc;

	// Store our product
	private Map<Integer, Whisky> products = new LinkedHashMap<>();

	@Override
	public void start(Future<Void> fut) {

		JsonObject config = new JsonObject().put("url", "jdbc:hsqldb:mem:db?shutdown=true").put("driver_class",
				"org.hsqldb.jdbcDriver");

		jdbc = JDBCClient.createShared(vertx, config, "My-Whisky-Collection");
		jdbc.getConnection(res -> {
			if (res.succeeded()) {
				SQLConnection connection = res.result();
				connection.execute("CREATE TABLE IF NOT EXISTS Whisky (id INTEGER IDENTITY, name varchar(100), "
						+ "origin varchar(100))", res2 -> {
							if (res2.succeeded()) {
								Void rs = res2.result();
							}
						});
			} else {
				// Failed to get connection - deal with it
			}
		});
		createSomeData();
		Router router = Router.router(vertx);
		router.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/html").end("<h1>Hello from my first Vert.x 3 application</h1>");
		});

		// Serve static resources from the /assets directory
		router.route("/assets/*").handler(StaticHandler.create("assets"));
		router.get("/api/whiskies").handler(this::getAll);
		router.route("/api/whiskies*").handler(BodyHandler.create());
		router.post("/api/whiskies").handler(this::addOne);
		router.put("/api/whiskies/:id").handler(this::updateOne);
		router.delete("/api/whiskies/:id").handler(this::deleteOne);

		vertx.createHttpServer().requestHandler(router).listen(
				// Retrieve the port from the configuration,
				// default to 8080.
				config().getInteger("http.port", 8080), result -> {
					if (result.succeeded()) {
						fut.complete();
					} else {
						fut.fail(result.cause());
					}
				});
	}

	// Create some product
	private void createSomeData() {
		Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
		products.put(bowmore.getId(), bowmore);
		Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
		products.put(talisker.getId(), talisker);
	}

	private void getAll(RoutingContext routingContext) {
		jdbc.getConnection(ar -> {
			SQLConnection connection = ar.result();
			connection.query("SELECT * FROM Whisky", result -> {
				List<Whisky> whiskies = result.result().getRows().stream().map(Whisky::new)
						.collect(Collectors.toList());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(whiskies));
				connection.close(); // Close the connection
			});
		});
	}

	private void addOne(RoutingContext routingContext) {
		final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(), Whisky.class);
		products.put(whisky.getId(), whisky);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(whisky));
	}

	private void updateOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			Integer idAsInteger = Integer.valueOf(id);
			Whisky whisky = products.get(idAsInteger);
			System.out.println("Original: " + whisky);
			final Whisky whiskyMod = Json.decodeValue(routingContext.getBodyAsString(), Whisky.class);
			System.out.println("Modificacion: " + whiskyMod);
			whisky.setName(whiskyMod.getName());
			whisky.setOrigin(whiskyMod.getOrigin());
			products.put(whisky.getId(), whisky);
		}
		routingContext.response().setStatusCode(201).end();
	}

	private void deleteOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			Integer idAsInteger = Integer.valueOf(id);
			products.remove(idAsInteger);
		}
		routingContext.response().setStatusCode(204).end();
	}
}
