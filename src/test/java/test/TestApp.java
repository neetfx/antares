package test;

import com.github.prchen.antares.starter.AntaresAutoConfiguration;
import com.github.prchen.antares.starter.AntaresScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@AntaresScan
@SpringBootApplication
@Import(AntaresAutoConfiguration.class)
public class TestApp {

    public static void main(String[] args) {
        SpringApplication.run(TestApp.class, args);
    }

}
