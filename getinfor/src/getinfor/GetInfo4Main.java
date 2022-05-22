package getinfor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author James
 *
 */
public class GetInfo4Main {

	public static  String url = "https://zhuisu.jncdc.cn/api/va/wandaRecord/getWandaVaccination";
	private static String parames = "?sfzjhm=codeNum&justCovid=1";
	private static final String localFile = "D:\\Users\\aaa.xlsx";
	private static File file = new File(localFile);

	/**
	 * ������Ϣ�ֶ�
	 */
	private static String[] valueOfKey = { "manufacturerName", "vaccinationTime", "deptDesc", "needNum" };

	private static final Logger logger = Logger.getGlobal();
//	�����Ϣ����
	private static Map<String, String> infodata = new HashMap<String, String>();
//	������Ϣ����
	private static Map<String, List<Map<String, String>>> jzdata = new HashMap<String, List<Map<String, String>>>();
	private static final String outfilePATH = "D:\\Users\\aaa_out.xlsx";
	private static File outfile = new File(outfilePATH);

	/**
	 * @param args
	 * @throws IOException
	 * @throws EncryptedDocumentException
	 */

	public static void main(String[] args) throws EncryptedDocumentException, IOException {
		List<String> numbers = readExcelGetNumbers();
		int count = getInforFromServer(numbers);
		writeInforToExcel();
	}

	/**
	 * @throws IOException
	 * @throws EncryptedDocumentException
	 * 
	 */
	private static void writeInforToExcel() throws EncryptedDocumentException, IOException {
		
		// ����������
		Workbook workbook = WorkbookFactory.create(outfile);
//					System.out.println("xssfWorkbook����" + workbook);
		// ��ȡ��һ��������(������±���listһ���ģ���0��ʼȡ��֮���Ҳ�����)
		Sheet sheet = workbook.getSheetAt(0);
//					// ��ȡ��һ�е�����
//					Row row = sheet.getRow(0);
//					System.out.println("row����" + row);
//					// ��ȡ���е�һ����Ԫ�������
//					Cell cell0 = row.getCell(0);
//					System.out.println("cello����" + cell0);
		int rownum = sheet.getLastRowNum();
//					logger.info("RowNum:"+rownum);
//					�ӵ����п�ʼ��ȡ����
		int readIndex = 3;
		for (; readIndex < rownum; readIndex++) {
			Row row = sheet.getRow(readIndex);
			int colNum = row.getLastCellNum();
//						logger.info("��"+(readIndex+1)+"��ȡ������"+colNum);
//						�����֤
			Cell cell4IdNumer = row.getCell(2);
			String id = cell4IdNumer.getStringCellValue().trim();
			logger.info("-----------��ʼд��:======"+id);
			List<Map<String, String>> jzxx = jzdata.get(id);
//			���ִ���
			if(jzxx==null) {
				Cell cell2Write = row.getCell(10);
				cell2Write.setCellValue("û����Ϣ");
				continue;
			}
			int jzcs = jzxx.size();
//			if(jzcs==3) {
//				logger.info("-----------������id:======"+id);
//
//			}
//			д�뵥Ԫ��λ��
			int[] pos = { 4, 5, 6, 7, 8, 9, 10 };
			String[] writeValues = { "", "", "", "", "", "", "" };
			for (int i = 0; i < jzcs; i++) {
				initValues(jzxx, i, i*2, writeValues);
			}
			for(int k = 0;k<pos.length;k++) {
				Cell cell2Write = row.getCell(pos[k]);
				cell2Write.setCellValue(writeValues[k]);
			}
		}
		FileOutputStream fileOut = new FileOutputStream(file);
		workbook.write(fileOut);
        fileOut.flush();
        fileOut.close();
	}

	/**
	 * @param jzxx
	 * @param i-������Ϣ
	 * @param jcStartIndex
	 * @param writeValues
	 */
	private static void initValues(List<Map<String, String>> jzxx, int i, int jcStartIndex, String[] writeValues) {
		for (int j = 0; j < valueOfKey.length; j++) {
			String value = jzxx.get(i).get(valueOfKey[j]);
			if(value==null||value.length()==0) {
				value="                ";
			}
			switch (j) {
//			���﹫˾(��������)
			case 0:
				value = value.substring(0, 4);
				writeValues[jcStartIndex + 1] = value;
				break;
//			����ʱ��
			case 1:
				value = value.substring(0, 10);
				writeValues[jcStartIndex] = value;
				break;
//			���ֵص�
			case 2:
				writeValues[jcStartIndex] =  writeValues[0]+value;
				break;
			case 3:
//			���ּ���
				break;
			default:
				break;
			}
		}
	}

	/**
	 * @param numbers
	 * @return ��������
	 */
	private static int getInforFromServer(List<String> numbers) {

		// ����Cookie�洢
		CookieStore cookieStore = new BasicCookieStore();
		// �½�Http�ͻ���
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		for (int i = 0; i < numbers.size(); i++) {
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
					if (responseEntity != null) {
						// System.out.println("��Ӧ���ݳ���Ϊ:" + responseEntity.getContentLength());
						// System.out.println("��Ӧ����Ϊ:" + EntityUtils.toString(responseEntity));
						String respData = EntityUtils.toString(responseEntity);
						logger.info("===respData==:"+respData);
						anlyzeRespAndWriteFile(respData, numbers.get(i));
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			parames = parames.replace(numbers.get(i), "codeNum");
//			try {
//				Thread.currentThread().sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}

		return 0;
	}

	/**
	 * ����ÿ����Ӧ������
	 * 
	 * @param respData
	 */
	private static void anlyzeRespAndWriteFile(String respData, String id) {
//    	manufacturerName=����������ά���＼�����޹�˾
		String data = respData.substring(respData.indexOf("["), respData.lastIndexOf("]") + 1);
		System.out.println("=========data:" + data);
		// ����һ��JsonParser
		JsonParser parser = new JsonParser();
		// ͨ��JsonParser������԰�json��ʽ���ַ���������һ��JsonElement����
		JsonElement el = parser.parse(data);

		// ��JsonElement����ת����JsonArray
		JsonArray jsonArray = null;
		if (el.isJsonArray()) {
			jsonArray = el.getAsJsonArray();
		}
		System.out.println("====jsonArray.size():" + jsonArray.size());
		Iterator it = jsonArray.iterator();
		List<Map<String, String>> info = new ArrayList<Map<String, String>>();
//		��ȡÿ������������Ϣ
		while (it.hasNext()) {
			Map<String, String> map = new HashMap<String, String>();
			JsonElement e = (JsonElement) it.next();
			JsonObject job = e.getAsJsonObject();
			for (short i = 0; i < 4; i++) {
				JsonElement value = job.get(valueOfKey[i]);
				if(value!=null&&!value.toString().equals("null")) {
					logger.info("====valueOfKey[i]===value"+value);
					map.put(valueOfKey[i], value.getAsString());
				}else {
					map.put(valueOfKey[i], "");
				}
			}
			info.add(map);
////			���繫˾
//			JsonElement manufacturerName = job.get("manufacturerName");
////			����ʱ��
//			JsonElement vaccinationTime  = job.get("vaccinationTime");
////			���ֵص�
//			JsonElement deptDesc = job.get("deptDesc");
////			���ּ���
//			JsonElement needNum  = job.get("needNum");
//			System.out.println("=========manufacturerName:"+manufacturerName.getAsString());
//			System.out.println("=========vaccinationTime:"+vaccinationTime.getAsString());
//			System.out.println("=========deptDesc:"+deptDesc.getAsString());
//			System.out.println("=========needNum:"+needNum.getAsString());
		}
		logger.info("===id:" + id + "===�н�����Ϣ����:" + info.size());
		jzdata.put(id, info);

	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	private static List<String> readExcelGetNumbers() {
		List<String> list = new ArrayList<String>();
		try {
			// ����������
			Workbook workbook = WorkbookFactory.create(file);
//			System.out.println("xssfWorkbook����" + workbook);
			// ��ȡ��һ��������(������±���listһ���ģ���0��ʼȡ��֮���Ҳ�����)
			Sheet sheet = workbook.getSheetAt(0);
			System.out.println("sheet����" + sheet);
//			// ��ȡ��һ�е�����
//			Row row = sheet.getRow(0);
//			System.out.println("row����" + row);
//			// ��ȡ���е�һ����Ԫ�������
//			Cell cell0 = row.getCell(0);
//			System.out.println("cello����" + cell0);
			int rownum = sheet.getLastRowNum();
//			logger.info("RowNum:"+rownum);
//			�ӵ����п�ʼ��ȡ����
			int readIndex = 3;
			for (; readIndex < rownum; readIndex++) {
				Row row = sheet.getRow(readIndex);
				int colNum = row.getLastCellNum();
//				logger.info("��"+(readIndex+1)+"��ȡ������"+colNum);
//				�����֤
				Cell cell4IdNumer = row.getCell(2);
				String id = cell4IdNumer.getStringCellValue().trim();
//				logger.info("��"+(readIndex+1)+"���֤��"+id);
//				if(id.equals("370121195701040552")) {
//					logger.info("��"+(readIndex+1)+"���֤˳����ȡ��");	
//				}
				infodata.put(String.valueOf(readIndex), id);
				list.add(id);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("����ȡ��������:" + list.size());
		return list;
	}

}
