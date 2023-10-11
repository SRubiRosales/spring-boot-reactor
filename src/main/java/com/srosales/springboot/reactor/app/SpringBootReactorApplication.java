package com.srosales.springboot.reactor.app;

import com.srosales.springboot.reactor.app.models.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploFlatMap();
	}

	public void ejemploFlatMap() throws Exception {
		List<String> listaUsuarios = new ArrayList<>();
		listaUsuarios.add("Sharon Rosales");
		listaUsuarios.add("Ruby Spark");
		listaUsuarios.add("Maria Fulana");
		listaUsuarios.add("Andrea Mengana");
		listaUsuarios.add("Bruce Willis");
		listaUsuarios.add("Bruce Lee");
		Flux.fromIterable(listaUsuarios)
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))// Method reference
				.flatMap(usuario -> {
					if (usuario.getNombre().equalsIgnoreCase("bruce")) {
						return Mono.just(usuario);
					} else {
						return Mono.empty();
					}
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;// Lambda
				}).subscribe(u -> log.info(u.toString()));
	}

	public void ejemploIterable() throws Exception {
		List<String> listaUsuarios = new ArrayList<>();
		listaUsuarios.add("Sharon Rosales");
		listaUsuarios.add("Ruby Spark");
		listaUsuarios.add("Maria Fulana");
		listaUsuarios.add("Andrea Mengana");
		listaUsuarios.add("Bruce Willis");
		listaUsuarios.add("Bruce Lee");
		Flux<String> nombres = Flux.fromIterable(listaUsuarios);
		Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))// Method reference
				.filter(usuario -> usuario.getNombre().toLowerCase().equals("bruce"))
				.doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacios");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;// Lambda
				});

		//nombres.subscribe(e -> log.info(e.toString()),
		usuarios.subscribe(e -> log.info(e.toString()),
				error -> log.error(error.getMessage()),
					new Runnable() {
						@Override
						public void run() {
							log.info("La ejecucion del observable ha finalizado con exito");
						}
					}
				);
	}
}
