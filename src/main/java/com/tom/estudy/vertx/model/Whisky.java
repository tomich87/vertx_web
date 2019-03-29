package com.tom.estudy.vertx.model;

import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class Whisky {

	private static final AtomicInteger COUNTER = new AtomicInteger();

	private final int id;

	private String name;

	private String origin;

	public Whisky(String name, String origin) {
		this.id = COUNTER.getAndIncrement();
		this.name = name;
		this.origin = origin;
	}
	
	public Whisky(JsonObject object) {
		Whisky temp = Json.decodeValue(object.encodePrettily(), Whisky.class);
		this.id = COUNTER.getAndIncrement();
		this.name = temp.getName();
		this.origin = temp.getOrigin();
	}

	public Whisky() {
		this.id = COUNTER.getAndIncrement();
	}

	public String getName() {
		return name;
	}

	public String getOrigin() {
		return origin;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	@Override
	public String toString() {
		return "Whisky [id=" + id + ", name=" + name + ", origin=" + origin + "]";
	}

}