package springcore.property.test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;

/**
 * @author James
 * @date 2022Äê5ÔÂ31ÈÕ
 * @version 1.0
 */
public class ProperGetFromEnvTest {
	public static void main(String[] args) {
		 AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		 ctx.register(AppConfig.class);
		 ctx.refresh();
		Environment env = ctx.getEnvironment();
		boolean containsMyProperty = env.containsProperty("my-property");
		System.out.println("Does my environment contain the 'my-property' property? " + containsMyProperty);
		AppConfig afg = ctx.getBean(AppConfig.class);
		TestBean tb = afg.testBean();
		System.out.println("Name:"+tb.getName());
	}
}
