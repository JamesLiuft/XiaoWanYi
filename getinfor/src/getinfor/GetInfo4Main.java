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
	 * 接种信息字段
	 */
	private static String[] valueOfKey = { "manufacturerName", "vaccinationTime", "deptDesc", "needNum" };

	private static final Logger logger = Logger.getGlobal();
//	身份信息数据
	private static Map<String, String> infodata = new HashMap<String, String>();
//	接种信息数据
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
		
		// 创建工作簿
		Workbook workbook = WorkbookFactory.create(outfile);
//					System.out.println("xssfWorkbook对象：" + workbook);
		// 读取第一个工作表(这里的下标与list一样的，从0开始取，之后的也是如此)
		Sheet sheet = workbook.getSheetAt(0);
//					// 获取第一行的数据
//					Row row = sheet.getRow(0);
//					System.out.println("row对象：" + row);
//					// 获取该行第一个单元格的数据
//					Cell cell0 = row.getCell(0);
//					System.out.println("cello对象：" + cell0);
		int rownum = sheet.getLastRowNum();
//					logger.info("RowNum:"+rownum);
//					从第三行开始读取数据
		int readIndex = 3;
		for (; readIndex < rownum; readIndex++) {
			Row row = sheet.getRow(readIndex);
			int colNum = row.getLastCellNum();
//						logger.info("第"+(readIndex+1)+"读取列数："+colNum);
//						拿身份证
			Cell cell4IdNumer = row.getCell(2);
			String id = cell4IdNumer.getStringCellValue().trim();
			logger.info("-----------开始写入:======"+id);
			List<Map<String, String>> jzxx = jzdata.get(id);
//			接种次数
			if(jzxx==null) {
				Cell cell2Write = row.getCell(10);
				cell2Write.setCellValue("没有信息");
				continue;
			}
			int jzcs = jzxx.size();
			if(jzcs==3) {
				logger.info("-----------三条的id:======"+id);

			}
//			写入单元格位置
			int[] pos = {  4, 5, 6, 7,8, 9,10 };
			String[] writeValues = { "", "", "", "", "", "", "" };
			
			for (int i = 0; i < jzcs; i++) {
//				这里有问题确定取的顺序需要,jzxx依次放的3，2，1次
				int writeIndex = 0;
//				首先获取的是第三或者第二条
				Map<String, String> jzxxdata = jzxx.get(i);
//				第二次信息
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
	 * @param i-剂次信息
	 * @param jcStartIndex
	 * @param writeValues
	 */
	private static void initValues(Map<String, String> jzxx, int jcsy, int jcStartIndex, String[] writeValues) {
		for (int j = 0; j < valueOfKey.length; j++) {
//			这里有问题确定取的顺序需要，jzxx依次放的3，2，1次
			String value = jzxx.get(valueOfKey[j]);
			if(value==null||value.length()==0) {
				value="                ";
			}
			switch (j) {
//			生物公司(疫苗类型)
			case 0:
				value = value.substring(0, 4);
				writeValues[jcStartIndex + 1] = value;
				break;
//			接种时间
			case 1:
				value = value.substring(0, 10);
				writeValues[jcStartIndex] = value;
				break;
//			接种地点
			case 2:
				writeValues[jcStartIndex] =  writeValues[jcStartIndex]+value;
				break;
			case 3:
//			接种剂次
				break;
			default:
				break;
			}
		}
	}

	

	/**
	 * 处理每条响应的数据
	 * 
	 * @param respData
	 */
	public static void anlyzeRespAndWriteFile(String respData, String id) {
//    	manufacturerName=北京科兴中维生物技术有限公司
		String data = respData.substring(respData.indexOf("["), respData.lastIndexOf("]") + 1);
		System.out.println("=========data:" + data);
		// 创建一个JsonParser
		JsonParser parser = new JsonParser();
		// 通过JsonParser对象可以把json格式的字符串解析成一个JsonElement对象
		JsonElement el = parser.parse(data);

		// 把JsonElement对象转换成JsonArray
		JsonArray jsonArray = null;
		if (el.isJsonArray()) {
			jsonArray = el.getAsJsonArray();
		}
		System.out.println("====jsonArray.size():" + jsonArray.size());
		Iterator it = jsonArray.iterator();
		List<Map<String, String>> info = new ArrayList<Map<String, String>>();
//		获取每条接种疫苗信息
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
////			疫苗公司
//			JsonElement manufacturerName = job.get("manufacturerName");
////			接种时间
//			JsonElement vaccinationTime  = job.get("vaccinationTime");
////			接种地点
//			JsonElement deptDesc = job.get("deptDesc");
////			接种剂次
//			JsonElement needNum  = job.get("needNum");
//			System.out.println("=========manufacturerName:"+manufacturerName.getAsString());
//			System.out.println("=========vaccinationTime:"+vaccinationTime.getAsString());
//			System.out.println("=========deptDesc:"+deptDesc.getAsString());
//			System.out.println("=========needNum:"+needNum.getAsString());
		}
		logger.info("===id:" + id + "===有接种信息条数:" + info.size());
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
			// 创建工作簿
			Workbook workbook = WorkbookFactory.create(file);
//			System.out.println("xssfWorkbook对象：" + workbook);
			// 读取第一个工作表(这里的下标与list一样的，从0开始取，之后的也是如此)
			Sheet sheet = workbook.getSheetAt(0);
			System.out.println("sheet对象：" + sheet);
//			// 获取第一行的数据
//			Row row = sheet.getRow(0);
//			System.out.println("row对象：" + row);
//			// 获取该行第一个单元格的数据
//			Cell cell0 = row.getCell(0);
//			System.out.println("cello对象：" + cell0);
			int rownum = sheet.getLastRowNum();
//			logger.info("RowNum:"+rownum);
//			从第三行开始读取数据
			int readIndex = 3;
			for (; readIndex < rownum; readIndex++) {
				Row row = sheet.getRow(readIndex);
				int colNum = row.getLastCellNum();
//				logger.info("第"+(readIndex+1)+"读取列数："+colNum);
//				拿身份证
				Cell cell4IdNumer = row.getCell(2);
				String id = cell4IdNumer.getStringCellValue().trim();
//				logger.info("第"+(readIndex+1)+"身份证："+id);
//				if(id.equals("370121195701040552")) {
//					logger.info("第"+(readIndex+1)+"身份证顺利读取！");	
//				}
				infodata.put(String.valueOf(readIndex), id);
				list.add(id);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("共读取数据条数:" + list.size());
		return list;
	}

}
