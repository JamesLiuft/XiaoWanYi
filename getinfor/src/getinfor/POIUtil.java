package getinfor;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.CellType;

//获取cell中的值并返回String类型工具类
public class POIUtil {
    public static String getCellValue(Cell cell) {
        String cellValue = "";
        if (null != cell) {
            //以下是判断数据类型
            switch (cell.getCellType()) {
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) { //判断是否为日期类型
                        Date date = cell.getDateCellValue();
                        DateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
                        cellValue = formater.format(date);
                    } else {
                        DecimalFormat df = new DecimalFormat("####.####");
                        cellValue = df.format(cell.getNumericCellValue());
                    }
                    break;
                case STRING:
                    cellValue = cell.getStringCellValue();
                    break;
                case BOOLEAN:
                    cellValue = cell.getBooleanCellValue() + "";
                    break;
                case BLANK: //空值
                    cellValue = "";
                case ERROR:
                    cellValue = "非法字符";
                    break;
            }
        }
        return cellValue;
    }
}