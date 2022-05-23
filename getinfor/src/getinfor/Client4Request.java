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
	 * @return 处理条数
	 */
	int getInforFromServer(List<String> numbers) {
		String url = "https://zhuisu.jncdc.cn/api/va/wandaRecord/getWandaVaccination";
		String parames = "?sfzjhm=codeNum&justCovid=1";
		// 建立Cookie存储
		CookieStore cookieStore = new BasicCookieStore();
		// 新建Http客户端
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		synchronized (parames) {
		for (int i = 0; i < numbers.size(); i++) {
			if(numbers.get(i)==null||numbers.get(i).equals("")) {
				continue;
			}
			parames = parames.replace("codeNum", numbers.get(i));
			try {
		// --->使用StringBuffer类进行字符拼接
		/* 发送GET请求 */
				// 创建get请求
				
				HttpGet httpGet = new HttpGet(url + parames);
				String cookie = "";
				// 创建响应模型，接收返回的响应实体
				CloseableHttpResponse response = null; // 未初始化
				try {
					// 配置信息
					RequestConfig requestConfig = RequestConfig.custom()
							// 设置连接超时时间(毫秒)
							.setConnectionRequestTimeout(5000)
							// 设置socket读写超时的时间
							.setSocketTimeout(5000)
							// 设置是否允许重定向
							.setRedirectsEnabled(true).build();
					// 将配置信息放入Get请求中
					httpGet.setConfig(requestConfig);
					// 响应模型接收这个请求
					response = httpClient.execute(httpGet);
					// 冲响应模型中获取响应实体
					HttpEntity responseEntity = response.getEntity();
					System.out.println("响应状态为:" + response.getStatusLine());
					// 判断响应实体
					StatusLine status = response.getStatusLine();
					int statusCode = status.getStatusCode();
					if(statusCode==200) {
						if (responseEntity != null) {
							// System.out.println("响应内容长度为:" + responseEntity.getContentLength());
							// System.out.println("响应内容为:" + EntityUtils.toString(responseEntity));
							String respData = EntityUtils.toString(responseEntity);
							logger.info("===respData==:"+respData);
							
							if(respData.contains("\"data\":null")||respData.contains("\"data\":[]")) {
								logger.info("=====查无结果或报错:"+numbers.get(i));
							}else {
								GetInfo4Main.anlyzeRespAndWriteFile(respData, numbers.get(i));
								
							}
						}else {
							httpGet.releaseConnection();
						}
					}else {
						logger.info("=====http请求状态异常:"+statusCode+"url:"+url + parames);
						httpGet.releaseConnection();
						logger.info(numbers.get(i)+"===第"+i+"条=====http请求状态异常:"+statusCode+"url:"+url + parames);

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
