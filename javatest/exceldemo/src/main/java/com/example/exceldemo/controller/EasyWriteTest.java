package com.example.exceldemo.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteWorkbook;
import com.example.exceldemo.entity.DemoData;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

public class EasyWriteTest {
    private List<DemoData> data() {
        List<DemoData> list = ListUtils.newArrayList();
        for (int i = 0; i < 10; i++) {
            DemoData data = new DemoData();
            data.setString("字符串" + i+1);
            data.setDate(new Date());
//            data.setDoubleData(0.56);
            list.add(data);
        }
        return list;
    }

    @Test
    public void simpleWrite() throws IOException {
        // 注意 simpleWrite在数据量不大的情况下可以使用（5000以内，具体也要看实际情况），数据量大参照 重复多次写入

        // 写法1 JDK8+
        // since: 3.0.0-beta1
        String fileName = "EasyTest.xlsx";
        // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
        // 如果这里想使用03 则 传入excelType参数即可
        EasyExcel.write(fileName, DemoData.class)
                .sheet("模板")
                .doWrite(() -> {
                    // 分页查询数据
                    return data();
                });
/*        FileOutputStream fos = new FileOutputStream(fileName,true);
        EasyExcel.write(fos,DemoData.class);*/
    }


    @Test
    public void repeatedWrite() {
        // 方法1.1: 如果写到同一个sheet 使用 try-with-resources @since 3.1.0
        String fileName =  "EasyTest.xlsx";        // 这里 需要指定写用哪个class去写
        try (ExcelWriter excelWriter = EasyExcel.write(fileName, DemoData.class).build()) {
            // 这里注意 如果同一个sheet只要创建一次
            WriteSheet writeSheet = EasyExcel.writerSheet("模板").build();
            // 去调用写入,这里我调用了五次，实际使用时根据数据库分页的总的页数来
            for (int i = 0; i < 5; i++) {
                // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
                List<DemoData> data = data();
                excelWriter.write(data, writeSheet);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        /*// 方法1.2: 如果写到同一个sheet 不使用 try-with-resources
        fileName = TestFileUtil.getPath() + "repeatedWrite" + System.currentTimeMillis() + ".xlsx";
        ExcelWriter writer = null;
        try {
            // 这里 需要指定写用哪个class去写
            writer = EasyExcel.write(fileName, DemoData.class).build();
            // 这里注意 如果同一个sheet只要创建一次
            WriteSheet writeSheet = EasyExcel.writerSheet("模板").build();
            // 去调用写入,这里我调用了五次，实际使用时根据数据库分页的总的页数来
            for (int i = 0; i < 5; i++) {
                // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
                List<DemoData> data = data();
                writer.write(data, writeSheet);
            }
        } finally {
            // 千万别忘记close 会帮忙关闭流
            if (writer != null) {
                writer.close();
            }
        }

        // 方法2.1: 如果写到不同的sheet 同一个对象 使用 try-with-resources @since 3.1.0
        fileName = TestFileUtil.getPath() + "repeatedWrite" + System.currentTimeMillis() + ".xlsx";
        // 这里 指定文件
        try (ExcelWriter excelWriter = EasyExcel.write(fileName, DemoData.class).build()) {
            // 去调用写入,这里我调用了五次，实际使用时根据数据库分页的总的页数来。这里最终会写到5个sheet里面
            for (int i = 0; i < 5; i++) {
                // 每次都要创建writeSheet 这里注意必须指定sheetNo 而且sheetName必须不一样
                WriteSheet writeSheet = EasyExcel.writerSheet(i, "模板" + i).build();
                // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
                List<DemoData> data = data();
                excelWriter.write(data, writeSheet);
            }
        }

        // 方法2.2: 如果写到不同的sheet 同一个对象 不使用 try-with-resources
        fileName = TestFileUtil.getPath() + "repeatedWrite" + System.currentTimeMillis() + ".xlsx";
        try {
            // 这里 指定文件
            writer = EasyExcel.write(fileName, DemoData.class).build();
            // 去调用写入,这里我调用了五次，实际使用时根据数据库分页的总的页数来。这里最终会写到5个sheet里面
            for (int i = 0; i < 5; i++) {
                // 每次都要创建writeSheet 这里注意必须指定sheetNo 而且sheetName必须不一样
                WriteSheet writeSheet = EasyExcel.writerSheet(i, "模板" + i).build();
                // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
                List<DemoData> data = data();
                writer.write(data, writeSheet);
            }
        } finally {
            // 千万别忘记close 会帮忙关闭流
            if (writer != null) {
                writer.close();
            }
        }

        // 方法3.1 如果写到不同的sheet 不同的对象 使用 try-with-resources @since 3.1.0
        fileName = TestFileUtil.getPath() + "repeatedWrite" + System.currentTimeMillis() + ".xlsx";
        // 这里 指定文件
        try (ExcelWriter excelWriter = EasyExcel.write(fileName).build()) {
            // 去调用写入,这里我调用了五次，实际使用时根据数据库分页的总的页数来。这里最终会写到5个sheet里面
            for (int i = 0; i < 5; i++) {
                // 每次都要创建writeSheet 这里注意必须指定sheetNo 而且sheetName必须不一样。这里注意DemoData.class 可以每次都变，我这里为了方便 所以用的同一个class
                // 实际上可以一直变
                WriteSheet writeSheet = EasyExcel.writerSheet(i, "模板" + i).head(DemoData.class).build();
                // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
                List<DemoData> data = data();
                excelWriter.write(data, writeSheet);
            }
        }

        // 方法3.2 如果写到不同的sheet 不同的对象 不使用 try-with-resources
        fileName = TestFileUtil.getPath() + "repeatedWrite" + System.currentTimeMillis() + ".xlsx";
        try {
            // 这里 指定文件
            writer = EasyExcel.write(fileName).build();
            // 去调用写入,这里我调用了五次，实际使用时根据数据库分页的总的页数来。这里最终会写到5个sheet里面
            for (int i = 0; i < 5; i++) {
                // 每次都要创建writeSheet 这里注意必须指定sheetNo 而且sheetName必须不一样。这里注意DemoData.class 可以每次都变，我这里为了方便 所以用的同一个class
                // 实际上可以一直变
                WriteSheet writeSheet = EasyExcel.writerSheet(i, "模板" + i).head(DemoData.class).build();
                // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
                List<DemoData> data = data();
                writer.write(data, writeSheet);
            }
        } finally {
            // 千万别忘记close 会帮忙关闭流
            if (writer != null) {
                writer.close();
            }
        }*/
    }

    @Test
    public void addWrite(){
        File templateFile = new File("EasyTest.xlsx");
        File destFile = new File("Easy.xlsx");
        ExcelWriter excelWriter=new ExcelWriter(new WriteWorkbook());
        try {
            if (templateFile.exists()) {
                //追加数据，目标文件与原始文件不能是同一个文件名
                //withTemplate()指定模板文件
                excelWriter = EasyExcel.write().withTemplate(templateFile)
                        //.file() 指定目标文件，不能与模板文件是同一个文件
                        .file(destFile).autoCloseStream(false).build();
            } else {
                excelWriter = EasyExcel.write(templateFile, DemoData.class)
                        .build();
            }
            WriteSheet writeSheet = EasyExcel.writerSheet("模板")
                    .build();
            excelWriter.write(data(), writeSheet);
        } finally {
            // 千万别忘记finish 会帮忙关闭流
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }

        if (destFile.exists()) {
            //删除原模板文件，新生成的文件变成新的模板文件
            templateFile.delete();
            destFile.renameTo(templateFile);
        }
    }
}
