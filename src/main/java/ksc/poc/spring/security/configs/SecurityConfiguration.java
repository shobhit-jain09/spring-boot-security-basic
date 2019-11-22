package ksc.poc.spring.security.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import ksc.poc.spring.security.jwt.filters.JwtRequestFilter;
import ksc.poc.spring.security.service.IMyUserDetailsService;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	/***
	 * - For In memory authentication it is not required.
	 * - For In memory database authentication it is required, but no external configuration required
	 */
	//@Autowired
	//private DataSource dataSource;
	
	/***
	 * This is the child of  Spring provide interface
	 * We can implement this interface to config our own implementation
	 */
	@Autowired
	private IMyUserDetailsService myUserDetailsService;
	
	@Autowired
	private JwtRequestFilter jwtRequestFilter;
	
	// AUTHENTICATION
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		// uncomment this if you want the only default user from application.properties file
		// super.configure(auth);
		
		// way 1 - in memory authentication
		/*
		auth
			.inMemoryAuthentication()
			.withUser("user").password("admin").roles("USER")
			.and()
			.withUser(User
						.withUsername("admin")
						.password("admin")
						.roles("ADMIN"))
			;
		*/
		
		// way 2 - using in-memory database and jdbc authentication with default schema and hard coded users
		// in case of any confusion refer notes
		/*
		auth
			.jdbcAuthentication() // jdbc authentication
			.dataSource(dataSource)
			.withDefaultSchema() // Spring sec default schema 
			.withUser(User.withUsername("user").password("admin").roles("USER")) // record 1 in default spring schema
			.withUser(User.withUsername("admin").password("admin").roles("ADMIN")) // record 2 in default spring schema
			;
		*/
		
		// way 3 - using in-memory database and jdbc authentication with configured schema(schema.sql) and configured users(data.sql)
		// in case of any confusion refer notes
		/*
		auth
			.jdbcAuthentication() // jdbc authentication
			.dataSource(dataSource)
			;
		*/
		
		// way 4 - using in-memory database  and jdbc  authentication with configured schema(schema.sql) and configured users(data.sql)
		// where we have pre existing user table not the default spring sec table
		// in case of any confusion refer notes
		/*
		auth
			.jdbcAuthentication() // jdbc authentication
			.dataSource(dataSource)
			.usersByUsernameQuery("SELECT username, password,enabled"
					+ " FROM users"
					+ " WHERE username  = ?")
			.authoritiesByUsernameQuery(" SELECT username, authority"
					+ " FROM authorities"
					+ " WHERE username  = ? ")
			;
		*/
		
		// way 5 - using sql database  and jdbc  authentication with configured schema(schema.sql) and configured users(data.sql)
		// where we have pre existing user table not the default spring sec table
		// in case of any confusion refer notes
		/*
		auth
			.userDetailsService(myUserDetailsService)
			.passwordEncoder(passwordEncoder());
			;
			*/
		// way 6 - almost same as way 5 only dfference is here we used DaoAuthenticationProvider to abstract configuration like encoder and userdetails service
		auth
			.authenticationProvider(authenticationProvider())
		;
	}
	
	
	/***
	 *  AUTHORIZATION
	 *  
	 *  Note :  Order of ant matcher is imp, 
	 *  it **MUST** be from least privileged role to highest privileged role 
	 */
	// 
	// 
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		/*
		http.authorizeRequests()
		.antMatchers("/admin/**").hasRole("ADMIN") // admin apis- all path in the current level( and since we have used /** all sub path of /admin ) for ADMIN role user
		.antMatchers("/user").hasAnyRole("USER","ADMIN") // user apis- all path in the current level for USER and ADMIN role user
		.antMatchers("/").permitAll() // all path in the current level of any role
		// .antMatchers("/**") // all path in the current level and sub path
		// .hasAnyRole("ADMIN")
		.and().formLogin(); // ?
		*/
		
		http.csrf().disable()
			.authorizeRequests()
				.antMatchers("/authenticate").permitAll() // whitelist this
				.antMatchers("/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated() // authenticate all others
			.and()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		
		http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
	}
	
	

	@Override
	public void configure(WebSecurity web) {
		web.ignoring().antMatchers("/h2-console/**");
	}


	@Bean
	public PasswordEncoder passwordEncoder() {
		// NoOpPasswordEncoder takes userid and password as String
		return NoOpPasswordEncoder.getInstance();
	}


	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setPasswordEncoder(passwordEncoder());
		provider.setUserDetailsService(myUserDetailsService);
		return provider;
	}
}
