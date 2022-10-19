package com.datawarehouse.excelgenerate.service.easyExcelSet;

import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.util.BooleanUtils;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

/**
 * @ClassName CommonCellWriteWriteHandler
 * @Description TODO
 * @Author xjy
 * @Date 2022/10/13 14:46
 * @Version 1.0
 **/
public class CommonCellWriteWriteHandler implements CellWriteHandler {
    @Override
    public void afterCellDispose(CellWriteHandlerContext context) {
        Cell cell = context.getCell();
        Workbook workbook = context.getWriteSheetHolder().getSheet().getWorkbook();
        CellStyle cellStyle = workbook.createCellStyle();
/*        Font cellFont = workbook.createFont();
//        cellFont.setBold(true);
        cellStyle.setFont(cellFont);*/
        cell.setCellStyle(cellStyle);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
//        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);//必须设置 否则无效
       /* // 背景设置为红色

        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        headWriteCellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short) 20);
        headWriteCellStyle.setWriteFont(headWriteFont);
        // 内容的策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        // 这里需要指定 FillPatternType 为FillPatternType.SOLID_FOREGROUND 不然无法显示背景颜色.头默认了 FillPatternType所以可以不指定
        contentWriteCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        // 背景绿色
        contentWriteCellStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        WriteFont contentWriteFont = new WriteFont();
        // 字体大小
        contentWriteFont.setFontHeightInPoints((short) 20);
        contentWriteCellStyle.setWriteFont(contentWriteFont);

        //设置边框样式
        contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);//细实线
        contentWriteCellStyle.setBorderTop(BorderStyle.THIN);
        contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
        contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);*/


/*        if (BooleanUtils.isTrue(context.getHead())) {
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
*//*            Workbook workbook = context.getWriteSheetHolder().getSheet().getWorkbook();
            CellStyle cellStyle = workbook.createCellStyle();*//*
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
        }*/
    }
}
