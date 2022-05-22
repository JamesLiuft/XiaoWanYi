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

//��ȡcell�е�ֵ������String���͹�����
public class POIUtil {
    public static String getCellValue(Cell cell) {
        String cellValue = "";
        if (null != cell) {
            //�������ж���������
            switch (cell.getCellType()) {
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) { //�ж��Ƿ�Ϊ��������
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
                case BLANK: //��ֵ
                    cellValue = "";
                case ERROR:
                    cellValue = "�Ƿ��ַ�";
                    break;
            }
        }
        return cellValue;
    }
}