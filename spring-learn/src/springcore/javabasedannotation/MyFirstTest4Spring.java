package springcore.javabasedannotation;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MyFirstTest4Spring {
	public static void main(String[] args) {
	    @SuppressWarnings("resource")
		ApplicationContext ctx = new AnnotationConfigApplicationContext(ConfigB.class);
	    // now both beans A and B will be available...
	    A a = ctx.getBean(A.class);
	    B b = ctx.getBean(B.class);
	    a.doSomething();
	    b.doSomething();
	}
}
