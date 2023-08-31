import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import weka.classifiers.functions.LinearRegression;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

@SpringBootApplication
@RestController
@EnableWebSecurity
public class SalesPredictorAPI extends WebSecurityConfigurerAdapter {
    private LinearRegression model;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SalesPredictorAPI.class, args);
    }

    public SalesPredictorAPI() throws Exception {
        // Load data from a CSV file
        DataSource source = new DataSource("sales_data.csv");
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);

        // Build a linear regression model
        model = new LinearRegression();
        model.buildClassifier(data);
    }

    @PostMapping("/predict")
    public double predictSales(@RequestBody double[] values) throws Exception {
        // Make a prediction for the input values
        return model.classifyInstance(new DenseInstance(1.0, values));
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // Add an in-memory user for authentication
        auth.userDetailsService(userDetailsService());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Configure security settings for the API
        http.csrf().disable()
                .authorizeRequests().anyRequest().authenticated()
                .and().httpBasic();
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        // Define an in-memory user for authentication
        return new InMemoryUserDetailsManager(
                User.withDefaultPasswordEncoder()
                        .username("user")
                        .password("password")
                        .roles("USER")
                        .build());
    }
}