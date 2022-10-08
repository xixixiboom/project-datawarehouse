package com.datawarehouse.excelgenerate.service.easyExcelSet;

import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.util.BooleanUtils;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

/**
 * @ClassName CustomHyperlinkAlreadyInCellWriteHandler
 * @Description TODO
 * @Author xjy
 * @Date 2022/10/6 3:11
 * @Version 1.0
 **/
public class HyperlinkAlreadyInCellWriteHandler implements CellWriteHandler {
    @Override
    public void afterCellDispose(CellWriteHandlerContext context) {
        Cell cell = context.getCell();
        // 这里可以对cell进行任何操作
        if(cell.getColumnIndex()==0&&cell.getRowIndex()==0){
            CreationHelper createHelper = context.getWriteSheetHolder().getSheet().getWorkbook().getCreationHelper();
            Hyperlink hyperlink = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
            hyperlink.setAddress("全表清单!A1");
            cell.setHyperlink(hyperlink);
        }
        /*if (BooleanUtils.isTrue(context.getHead())) {
            WriteCellData<?> cellData = context.getFirstCellData();
            WriteCellStyle writeCellStyle = cellData.getOrCreateStyle();
//            headWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            WriteFont headWriteFont = new WriteFont();
            headWriteFont.setFontName("宋体");
            headWriteFont.setFontHeightInPoints((short)400);
            headWriteFont.setBold(true);
            writeCellStyle.setWriteFont(headWriteFont);
            //自动换行
            writeCellStyle.setWrapped(false);

            // 自定义背景色
            int r = 0;
            int g = 112;
            int b = 192;

            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFPalette palette = wb.getCustomPalette();
            HSSFColor hssfColor = palette.findSimilarColor(r, g, b);

            // 背景色与填充
            writeCellStyle.setFillForegroundColor(hssfColor.getIndex());
            writeCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
            writeCellStyle.setBorderLeft(BorderStyle.THIN);
            writeCellStyle.setBorderTop(BorderStyle.THIN);
            writeCellStyle.setBorderRight(BorderStyle.THIN);
            writeCellStyle.setBorderBottom(BorderStyle.THIN);


        }else if(BooleanUtils.isNotTrue(context.getHead())){
            Workbook workbook = context.getWriteSheetHolder().getSheet().getWorkbook();
            CellStyle cellStyle = workbook.createCellStyle();
            WriteCellData<?> cellData = context.getFirstCellData();
            // 这里需要去cellData 获取样式
            // 很重要的一个原因是 WriteCellStyle 和 dataFormatData绑定的 简单的说 比如你加了 DateTimeFormat
            // ，已经将writeCellStyle里面的dataFormatData 改了 如果你自己new了一个WriteCellStyle，可能注解的样式就失效了
            // 然后 getOrCreateStyle 用于返回一个样式，如果为空，则创建一个后返回
            WriteCellStyle writeCellStyle = cellData.getOrCreateStyle();
//            headWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            WriteFont headWriteFont = new WriteFont();
            headWriteFont.setFontName("仿宋");
            headWriteFont.setFontHeightInPoints((short)400);
            headWriteFont.setBold(false);
            writeCellStyle.setWriteFont(headWriteFont);
            //自动换行
            writeCellStyle.setWrapped(false);
            writeCellStyle.setBorderLeft(BorderStyle.THIN);
            writeCellStyle.setBorderTop(BorderStyle.THIN);
            writeCellStyle.setBorderRight(BorderStyle.THIN);
            writeCellStyle.setBorderBottom(BorderStyle.THIN);

            Sheet sheet = context.getWriteSheetHolder().getSheet();
            sheet.setDefaultRowHeight((short)400);
        }
*/
    }
}
