package springcore.javabasedannotation.weaklycoupled;

public class TransferServiceImpl implements TransferService {

	public TransferServiceImpl(AccountRepository accountRepository) {
		
	}

	@Override
	public void transfer(double d, String string, String string2) {
		System.out.println("====transferMsg:"+d+"===="+string+"===="+string2);
	}

}
