# Spring Boot Security Authorization

## This POC includes in-memory authentication using any schema[REcommended] and default user from the sql fils [Recommended over hard coding]

```java
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	/***
	 * 
	 * - For In memory database authentication it is required, but no external configuration required
	 */
	@Autowired
	private DataSource dataSource;
	
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
		
		// way 2 - using jdbc authentication with default schema and hard coded users
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
		
		// way 3 - using jdbc authentication with configured schema(schema.sql) and configured users(data.sql)
		// in case of any confusion refer notes
		/*
		auth
			.jdbcAuthentication() // jdbc authentication
			.dataSource(dataSource)
			;
		*/
		
		// way 4 - using jdbc authentication with configured schema(schema.sql) and configured users(data.sql)
		// where we have pre existing user table not the default spring sec table
		// in case of any confusion refer notes
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
		http.authorizeRequests()
		.antMatchers("/admin").hasRole("ADMIN") // admin apis- all path in the current level for ADMIN role user
		.antMatchers("/user").hasAnyRole("USER","ADMIN") // user apis- all path in the current level for USER and ADMIN role user
		.antMatchers("/").permitAll() // all path in the current level of any role
		// .antMatchers("/**") // all path in the current level and sub path
		// .hasAnyRole("ADMIN")
		.and().formLogin(); // ?
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		// NoOpPasswordEncoder takes userid and password as String
		return NoOpPasswordEncoder.getInstance();
	}

}

```

## Ques - 
In the classpath data.sql file we insert authorities as ROLE_USER, ROLE_ADMIN <br />
But in the SecurityConfiguration we give access to the rols USER , ADMIN <by />
How does it works ?

## POC endpoints
http://localhost:8080 - unauthenticated user<br />
http://localhost:8080/user - authenticated user with the role either ADMIN or USER <br />
http://localhost:8080/admin - authenticated user with the ADMIN role <br />
