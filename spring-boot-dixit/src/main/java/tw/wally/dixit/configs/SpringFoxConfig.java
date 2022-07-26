package tw.wally.dixit.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;
import tw.wally.dixit.controllers.DixitController;

import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import static springfox.documentation.spi.DocumentationType.OAS_30;

/**
 * @author - wally55077@gmail.com
 */
@Configuration
@EnableOpenApi
public class SpringFoxConfig {

    @Bean
    public Docket api() {
        return new Docket(OAS_30)
                .apiInfo(apiInfo())
                .select()
                .apis(basePackage(DixitController.class.getPackageName()))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Dixit-as-a-service")
                .description("回合制卡牌類益智遊戲")
                .contact(new Contact("Wally","http://localhost:8080","localhost8080@gmail.com"))
                .version("1.0.0")
                .build();
    }
}
