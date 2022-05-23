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
		Client4Request client =new Client4Request();
		int count = client.getInforFromServer(numbers);
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
			if(jzcs==3) {
				logger.info("-----------������id:======"+id);

			}
//			д�뵥Ԫ��λ��
			int[] pos = {  4, 5, 6, 7,8, 9,10 };
			String[] writeValues = { "", "", "", "", "", "", "" };
			
			for (int i = 0; i < jzcs; i++) {
//				����������ȷ��ȡ��˳����Ҫ,jzxx���ηŵ�3��2��1��
				int writeIndex = 0;
//				���Ȼ�ȡ���ǵ������ߵڶ���
				Map<String, String> jzxxdata = jzxx.get(i);
//				�ڶ�����Ϣ
				if(jzcs==2&&i==0) {
					writeIndex = 2;
				}else if(jzcs==3&&i==0) {
					writeIndex =4;
				}else if(jzcs==2&&i==1){
					writeIndex =0;
				}else if(jzcs==3&&i==1) {
					writeIndex = 2;
				}else {
					writeIndex =0;
				}
				initValues(jzxxdata, i, writeIndex, writeValues);
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
	private static void initValues(Map<String, String> jzxx, int jcsy, int jcStartIndex, String[] writeValues) {
		for (int j = 0; j < valueOfKey.length; j++) {
//			����������ȷ��ȡ��˳����Ҫ��jzxx���ηŵ�3��2��1��
			String value = jzxx.get(valueOfKey[j]);
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
				writeValues[jcStartIndex] =  writeValues[jcStartIndex]+value;
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
	 * ����ÿ����Ӧ������
	 * 
	 * @param respData
	 */
	public static void anlyzeRespAndWriteFile(String respData, String id) {
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
