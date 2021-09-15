package org.dailycodebuffer;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.jaxrs.PathParam;

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.query.QueryResult;

import java.util.List;

@Path("/cars")
@ApplicationScoped
@Produces("application/json")

public class GreetingController {

	String host = "127.0.0.1";
	String username = "admin";
	String password = "admin";
	String bucketName = "CouchCars";

	Cluster cluster = Cluster.connect(host, username, password);

	Bucket bucket = cluster.bucket(bucketName);

	Collection collection = bucket.defaultCollection();

	@GET
	@Path("/greeting")
	public String hello() {
		return "Hello Customer";
	}

	@POST
	@Path("/addCar")
	@Produces(MediaType.TEXT_PLAIN)
	public Response addCar(Car car) {
		collection.insert(car.getId(), car);
		return Response.ok(car).status(201).build();
	}

	@PUT
	@Path("/{id}")
	public Response UpdateCar(@PathParam("id") String id, Car car) {
		car.setId(id);
		collection.replace(id, car);
		return Response.ok(car).status(201).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Car> getAllCars() {
		QueryResult result = cluster.query("SELECT id, brand, modelName, price FROM CouchCars");
		System.out.println("Output: " + result.rowsAsObject());
		return result.rowsAs(Car.class);
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Car getCar(@PathParam("id") String id) {
		GetResult result = collection.get(id);
		return result.contentAs(Car.class);
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteCar(@PathParam("id") String id) {
		try {
			collection.remove(id);
		} catch (CouchbaseException e) {
			throw new WebApplicationException("Car with id " + id + "not found.", 404);

		}
		return Response.status(204).build();

	}

	@GET
	@Path("/Brand/{brand}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Car> getCarByBrand(@PathParam("brand") String brand) {
		QueryResult result = cluster
				.query("SELECT id, brand, modelName, price FROM CouchCars WHERE brand='" + brand + "' ");
		return result.rowsAs(Car.class);
	}

	@GET
	@Path("/Brand/{brand}/{modelName}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Car> getCar(@PathParam("brand") String brand, @PathParam("modelName") String modelName) {
		QueryResult result = cluster.query("SELECT id, brand, modelName, price FROM CouchCars WHERE brand='" + brand
				+ "' AND modelName='" + modelName + "' ");
		return result.rowsAs(Car.class);
	}

}
