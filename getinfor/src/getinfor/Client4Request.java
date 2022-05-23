package getinfor;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Client4Request {
	private static final Logger logger = Logger.getGlobal();

	/**
	 * @param numbers
	 * @return ��������
	 */
	int getInforFromServer(List<String> numbers) {
		String url = "https://zhuisu.jncdc.cn/api/va/wandaRecord/getWandaVaccination";
		String parames = "?sfzjhm=codeNum&justCovid=1";
		// ����Cookie�洢
		CookieStore cookieStore = new BasicCookieStore();
		// �½�Http�ͻ���
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		synchronized (parames) {
		for (int i = 0; i < numbers.size(); i++) {
			if(numbers.get(i)==null||numbers.get(i).equals("")) {
				continue;
			}
			parames = parames.replace("codeNum", numbers.get(i));
			try {
		// --->ʹ��StringBuffer������ַ�ƴ��
		/* ����GET���� */
				// ����get����
				
				HttpGet httpGet = new HttpGet(url + parames);
				String cookie = "";
				// ������Ӧģ�ͣ����շ��ص���Ӧʵ��
				CloseableHttpResponse response = null; // δ��ʼ��
				try {
					// ������Ϣ
					RequestConfig requestConfig = RequestConfig.custom()
							// �������ӳ�ʱʱ��(����)
							.setConnectionRequestTimeout(5000)
							// ����socket��д��ʱ��ʱ��
							.setSocketTimeout(5000)
							// �����Ƿ������ض���
							.setRedirectsEnabled(true).build();
					// ��������Ϣ����Get������
					httpGet.setConfig(requestConfig);
					// ��Ӧģ�ͽ����������
					response = httpClient.execute(httpGet);
					// ����Ӧģ���л�ȡ��Ӧʵ��
					HttpEntity responseEntity = response.getEntity();
					System.out.println("��Ӧ״̬Ϊ:" + response.getStatusLine());
					// �ж���Ӧʵ��
					StatusLine status = response.getStatusLine();
					int statusCode = status.getStatusCode();
					if(statusCode==200) {
						if (responseEntity != null) {
							// System.out.println("��Ӧ���ݳ���Ϊ:" + responseEntity.getContentLength());
							// System.out.println("��Ӧ����Ϊ:" + EntityUtils.toString(responseEntity));
							String respData = EntityUtils.toString(responseEntity);
							logger.info("===respData==:"+respData);
							
							if(respData.contains("\"data\":null")||respData.contains("\"data\":[]")) {
								logger.info("=====���޽���򱨴�:"+numbers.get(i));
							}else {
								GetInfo4Main.anlyzeRespAndWriteFile(respData, numbers.get(i));
								
							}
						}else {
							httpGet.releaseConnection();
						}
					}else {
						logger.info("=====http����״̬�쳣:"+statusCode+"url:"+url + parames);
						httpGet.releaseConnection();
						logger.info(numbers.get(i)+"===��"+i+"��=====http����״̬�쳣:"+statusCode+"url:"+url + parames);

						System.exit(0);
					}
					
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				httpGet.reset();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			
			parames = parames.replace(numbers.get(i), "codeNum");
			
//			try {
//				Thread.currentThread().sleep(5000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		}
		return 0;
	}
}
