package com.datawarehouse.excelgenerate.service.easyExcelSet;


import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;

import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @ClassName CustomHyperlinkCellWriteHandler
 * @Description TODO
 * @Author xjy
 * @Date 2022/10/6 0:09
 * @Version 1.0
 **/

public class HyperlinkTableListCellWriteHandler implements CellWriteHandler {
    public List<String> listSheet;
    public HyperlinkTableListCellWriteHandler(List<String> listSheet){
        this.listSheet = listSheet;
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(HyperlinkTableListCellWriteHandler.class);

    @Override
    public void afterCellDispose(CellWriteHandlerContext context) {

        Cell cell = context.getCell();
        // 这里可以对cell进行任何操作
//        LOGGER.info("第{}行，第{}列写入完成。", cell.getRowIndex(), cell.getColumnIndex());
        if(listSheet!=null&&listSheet.size()>0){
            if(cell.getColumnIndex()==2){
                String sTable = listSheet.get(0);

                if(!sTable.equals("此处无需超链接")){
                    CreationHelper createHelper = context.getWriteSheetHolder().getSheet().getWorkbook().getCreationHelper();
                    Hyperlink hyperlink = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
                    hyperlink.setAddress(sTable+"!A1");
                    cell.setHyperlink(hyperlink);
                }

                listSheet.remove(0);
                /*Workbook workbook = context.getWriteSheetHolder().getSheet().getWorkbook();
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setBorderLeft(BorderStyle.THIN);
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);
                cellStyle.setBorderBottom(BorderStyle.THIN);
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);//必须设置 否则无效*/
            }
        }
    }
}
