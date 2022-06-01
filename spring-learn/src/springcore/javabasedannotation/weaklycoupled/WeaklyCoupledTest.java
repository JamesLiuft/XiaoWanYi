package springcore.javabasedannotation.weaklycoupled;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class WeaklyCoupledTest {
	public static void main(String[] args) {
	    ApplicationContext ctx = new AnnotationConfigApplicationContext(SystemTestConfig.class);
	    TransferService transferService = ctx.getBean(TransferService.class);
	    transferService.transfer(100.00, "A123", "C456");
	}
}