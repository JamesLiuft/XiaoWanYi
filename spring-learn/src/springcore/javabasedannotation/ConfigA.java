package springcore.javabasedannotation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
class A{
	public void doSomething() {
		System.out.println("HI I am A");
	}
}
@Configuration
public class ConfigA {

    @Bean
    public A a() {
        return new A();
    }
}
