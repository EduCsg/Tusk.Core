package com.hydra.core.config;

import com.hydra.core.dtos.UserDto;
import com.hydra.core.utils.JwtUtils;
import com.hydra.core.utils.ValidationUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class AuthFilter extends OncePerRequestFilter {

	private static final String PUBLIC_TOKEN = System.getenv("PUBLIC_TOKEN");

	@Override
	protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		String authorizationHeader = request.getHeader("Authorization");

		if (ValidationUtils.notEmpty(authorizationHeader) && authorizationHeader.equals(PUBLIC_TOKEN)) {
			filterChain.doFilter(request, response);
			return;
		}

		if (ValidationUtils.isEmpty(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
			return;
		}

		String token = authorizationHeader.substring(7).trim();

		if (ValidationUtils.notEmpty(token) && JwtUtils.validateToken(token)) {
			UserDto userDto = JwtUtils.parseTokenToUser(token);
			List<String> roles = JwtUtils.getRolesByToken(token);

			List<GrantedAuthority> authorities = new ArrayList<>();
			if (ValidationUtils.notEmpty(roles))
				authorities = roles.stream() //
								   .map(r -> ValidationUtils.isEmpty(r) ? "" : r) //
								   .map(SimpleGrantedAuthority::new) //
								   .collect(Collectors.toList());

			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDto.username(),
					token, authorities);
			SecurityContextHolder.getContext().setAuthentication(auth);

			filterChain.doFilter(request, response);
		} else {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
		}
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();

		List<String> publicPaths = new ArrayList<>();
		publicPaths.add("/actuator/health");
		publicPaths.add("/auth/login");
		publicPaths.add("/auth/register");

		if (publicPaths.contains(path))
			return true;

		List<String> startsWithPaths = new ArrayList<>();
		startsWithPaths.add("/swagger-ui");
		startsWithPaths.add("/v3/api-docs");

		return startsWithPaths.stream().anyMatch(path::startsWith);
	}

}