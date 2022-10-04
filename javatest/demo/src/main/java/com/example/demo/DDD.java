package com.example.demo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DDD {
    public static void main( String[] args ){
/*        // 按指定模式在字符串查找
        String line = "P1.CNTRCT_NO=P4.CNTRCT_NO AND P4.START_DT<=DATE'${batch_date}' AND P4.END_DT>DATE'${batch_date}' AND P4.CNTRCT_STATUS_TYPE_CD='0039' AND P4.CNTRCT_STATUS_CD='04'";
//        String pattern = "^P\\d\\.\\S{1,15}\\=\\'\\S{1,15}\\'$\n";
        String pattern = "P\\d\\.\\S{1,25}(\\s)?=(\\s)?\\'\\S{1,25}\\'";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        // 现在创建 matcher 对象
        Matcher m = r.matcher(line);
        while(m.find()) {
            String str = m.group();
            int i = str.indexOf("'");
            int j = str.lastIndexOf("'");
            int k = str.indexOf("=");
            System.out.println(str.substring(i+1,j));
            System.out.println(str.substring(3,k));
//            System.out.println(ffasdf);*/

/*        String line = "ods_01_iv_d0_1_d";
        Pattern n = Pattern.compile("ods_"+"01_iv"+"_d\\d_(i|f)_\\w");
        Matcher mn = n.matcher(line);
        System.out.println(mn.matches());*/

       /* String pattern = "(P|p|T|t)[0-9]{1,2}\\.SRC_TAB(\\))(\\s)?=(\\s)?'\\S{1,25}\\'";
        String line = "upper(t1.src_tab)='M_3agdlas' and t1.ci_srt_gasd = '1';".toUpperCase();*/

        String pattern = "(P|p|T|t)[0-9]{1,2}\\.SRC_TAB(\\))?(\\s)?in(\\s)?'\\S{1,25}\\'";
        String line = "upper(t1.src_tab)='M_3agdlas' and t1.ci_srt_gasd = '1';".toUpperCase();
        pattern = pattern.toUpperCase();
//        String pattern = "(P|p|T|t)\\d\\.SRC_TAB(\\))(\\s)?=(\\s)?'\\S{1,25}\\'";
        Pattern p = Pattern.compile(pattern);
        Matcher mn = p.matcher(line);
        while(mn.find()) {
            String str = mn.group();
            System.out.println(str);
        }
    }



}
