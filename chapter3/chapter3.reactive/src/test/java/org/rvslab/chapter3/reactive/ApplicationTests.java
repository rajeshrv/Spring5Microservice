package org.rvslab.chapter3.reactive;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import reactor.test.subscriber.ScriptedSubscriber;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ApplicationTests {

	private WebClient webClient;

	@LocalServerPort
	private int port;

	@Before
	public void setup() {
		
		this.webClient = WebClient.create(new ReactorClientHttpConnector());
		
	}
 
	
	@Test
	public void customArgument() throws Exception {
		ClientRequest<Void> request = ClientRequest
									.GET("http://localhost:{port}", this.port)
									.build();

		Mono<Greet> result = webClient
				   .exchange(request)
				   .then(response -> response.bodyToMono(Greet.class));
		
		//https://github.com/bclozel/spring-boot-web-reactive
		 
		result.subscribe(greet -> System.out.println(greet.getMessage()));
		
		ScriptedSubscriber.<Greet>create()
							.consumeNextWith(content -> {
							assertThat(content.getMessage()).contains("Hello World!");
							})
							.expectComplete()
							.verify(result);
	}
	
}


