package springcore.javabasedannotation.weaklycoupled;

public class DefaultRepositoryConfig implements RepositoryConfig {

	@Override
	public AccountRepository accountRepository() {
		return null;
	}

}
