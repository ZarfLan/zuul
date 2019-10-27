package com.jiuzhe.app.gateway;

import com.google.common.base.Splitter;
import com.jiuzhe.app.gateway.filter.AuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringCloudApplication
@EnableZuulProxy
public class GatewayZuulApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayZuulApplication.class, args);
	}

	@Value("${ignore-uri}")
	private String strIgnoreUris;

	@Bean
	public AuthFilter userAuthFilter() {
		List<String> ignoreUris = Splitter.on('-')
				.trimResults()
				.omitEmptyStrings()
				.splitToList(strIgnoreUris);
		return new AuthFilter(ignoreUris);
	}
}
