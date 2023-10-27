package com.srosales.springboot.reactor.app;

import com.srosales.springboot.reactor.app.models.Comentarios;
import com.srosales.springboot.reactor.app.models.Usuario;
import com.srosales.springboot.reactor.app.models.UsuarioComentarios;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploBackPressure();
	}

	public void ejemploBackPressure() {
		Flux.range(1, 10)
				.log()
				.limitRate(3)
				.subscribe();
	}

	public void ejemploIntervaloDesdeCreate() {
		Flux.create(emmiter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				private Integer counter = 0;
				@Override
				public void run() {
					emmiter.next(++counter);
					if (counter == 10) {
						timer.cancel();
						emmiter.complete();
					}
					if (counter == 5) {
						emmiter.error(new InterruptedException("Error en elemento 5 del flux"));
					}
				}
			}, 1000, 1000);
		})
				.doOnComplete(() -> log.info("Fin del flujo"))
				.subscribe(next -> log.info(next.toString()),
						error -> log.error(error.getMessage()),
						() -> log.info("Fin del flux"));
	}

	public void ejemploIntervaloInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(latch::countDown)
				.flatMap(i -> {
					if (i >= 5) {
						return Flux.error(new InterruptedException("5 es el límite"));
					}
					return Flux.just(i);
				})
				.map(i -> "Hola " + i)
				.retry(2)
				//.doOnNext(s -> log.info(s))
				.subscribe(log::info, e-> log.error(e.getMessage()));
		latch.await();
	}

	public void ejemploInterval() {
		Flux<Integer> rango = Flux.range(1, 10);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
		rango.zipWith(retraso, (r, d) -> r)
				.doOnNext(i -> log.info(i.toString()))
				.blockLast();
	}

	public void ejemploDelayElements() {
		Flux<Integer> rango = Flux.range(1, 10)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));
		//rango.subscribe();
		rango.blockLast();
	}

	public void ejemploZipWithRange() {
		Flux.just(1, 2, 3, 4)
				.map(i -> (i*2))
				.zipWith(Flux.range(0, 4), (uno, dos) ->
						String.format("Primer Flux %d, Segundo Flux %d", uno, dos))
				.subscribe(texto -> log.info(texto));
	}

	public void ejemploUsuarioComentariosZipWith() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() ->  new Usuario("Jonh", "Doe"));
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola mundo");
			comentarios.addComentario("Hello world");
			comentarios.addComentario("Good bye");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono.zipWith(comentariosUsuarioMono, (usuario, comentariosUsuario) ->
						new UsuarioComentarios(usuario, comentariosUsuario));
		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}
	public void ejemploUsuarioComentariosZipWith2() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() ->  new Usuario("Jonh", "Doe"));
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola mundo");
			comentarios.addComentario("Hello world");
			comentarios.addComentario("Good bye");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono.zipWith(comentariosUsuarioMono)
				.map(tuple -> {
					Usuario u = tuple.getT1();
					Comentarios c = tuple.getT2();
					return new UsuarioComentarios(u, c);
				});
		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}
	public void ejemploUsuarioComentariosFlatMap() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() ->  new Usuario("Jonh", "Doe"));
		Mono<Comentarios> comentariosUsuario = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola mundo");
			comentarios.addComentario("Hello world");
			comentarios.addComentario("Good bye");
			return comentarios;
		});

		usuarioMono.flatMap(u -> comentariosUsuario.map(c -> new UsuarioComentarios(u, c)))
				.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploCollectList() throws Exception {
		List<Usuario> listaUsuarios = new ArrayList<>();
		listaUsuarios.add(new Usuario("Sharon","Rosales"));
		listaUsuarios.add(new Usuario("Ruby", "Spark"));
		listaUsuarios.add(new Usuario("Maria","Fulana"));
		listaUsuarios.add(new Usuario("Andrea","Mengana"));
		listaUsuarios.add(new Usuario("Bruce","Willis"));
		listaUsuarios.add(new Usuario("Bruce","Lee"));
		Flux.fromIterable(listaUsuarios)
				.collectList()
				.subscribe(lista -> {
					lista.forEach(item -> log.info(item.toString()));
				});
	}
	public void ejemploToString() throws Exception {
		List<Usuario> listaUsuarios = new ArrayList<>();
		listaUsuarios.add(new Usuario("Sharon","Rosales"));
		listaUsuarios.add(new Usuario("Ruby", "Spark"));
		listaUsuarios.add(new Usuario("Maria","Fulana"));
		listaUsuarios.add(new Usuario("Andrea","Mengana"));
		listaUsuarios.add(new Usuario("Bruce","Willis"));
		listaUsuarios.add(new Usuario("Bruce","Lee"));
		Flux.fromIterable(listaUsuarios)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if (nombre.contains("bruce".toUpperCase())) {
						return Mono.just(nombre);
					} else {
						return Mono.empty();
					}
				})
				.map(nombre -> {
					return nombre.toLowerCase();// Lambda
				}).subscribe(u -> log.info(u.toString()));
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
