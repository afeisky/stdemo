/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package afeistock;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.UUID;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author chaofei.wu
 */
public class Down {

    private String sina_dir;
    private String htm_key_begin = "历史交易begin-->";
    private String htm_key_end = "<!--历史交易end";
    private boolean needSaveDownloadHtmlData = false;

    public Down() {
        sina_dir = Paths.get(Global.workDir, "\\DownloadSina").toString();
    }

    String sse_dirname = "Download_sse";

    public void get_sina_from_html_file() {
        Global.logd("--Data.run");
        sina_dir = Paths.get(Global.workDir, "\\DownloadData").toString();
        sse_download_json(sse_dirname);
    }

    public void download_sina_history() {
        Global.logd("--Data.run");
        sina_dir = Paths.get(Global.workDir, "\\DownloadSina").toString();
        //parseSina(sina_dir);
        sina_download_parse_htm();
    }
    /*
    private String parser_html_from_sina(String filepathname) {
        try {
            // read file content from file
            File f = new File(filepathname);
            String data = "";
            if (f.isFile() && f.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(f), "gbk");
                BufferedReader reader = new BufferedReader(read);
                String line;
                while ((line = reader.readLine()) != null) {
                    data += line;
                }
                read.close();
            } else {
                return "";
            }
            int begin = data.indexOf(htm_key_begin);
            int end = data.indexOf(htm_key_end);
            System.out.println(data);
            System.out.println(f.getName() + ": " + begin + "," + end);
            if (begin > 0 && end > 0 && end > begin) {
                data = data.substring(begin + htm_key_begin.length(), end);
            }
            if (data.length() > 0) {
                try {
                    String strkey1 = "<table id=\"FundHoldSharesTable\">";
                    begin = data.indexOf(strkey1);
                    if (begin > 0) {
                        data = data.substring(begin, data.length());
                    }
                    strkey1 = "</table>";
                    end = data.indexOf(strkey1);
                    if (end > 0) {
                        data = data.substring(0, end + strkey1.length());
                    }
                    File dir = new File(sina_dir);
                    String out_dir = Paths.get(sina_dir, "../" + dir.getName() + "out").toString();
                    System.out.println("fileName: " + out_dir);
                    dir = new File(out_dir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    if (!dir.exists()) {
                        System.out.println("Error: create dir fail! [" + sina_dir + "]");
                        return "";
                    }
                    String fileName = Paths.get(out_dir, f.getName().replace(".htm", ".data")).toString();
                    OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fileName), "gbk");
                    out.write(data);
                    out.close();
                    return fileName;
                } catch (Exception e) {
                    return "";
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
     */
    String sse_url = "http://yunhq.sse.com.cn:32041/v1/sh1/list/exchange/equity?select=code%2Cname%2Copen%2Chigh%2Clow%2Clast%2Cprev_close%2Cchg_rate%2Cvolume%2Camount%2Ctradephase%2Cchange%2Camp_rate&order=&begin=0&end=5000&_=1501664482391";

    private boolean sse_download_json(String sse_dirname) {
        String sse_pathname = Paths.get(Global.workDir, sse_dirname).toString();
        File sse_dir = new File(sse_pathname);
        if (!sse_dir.exists()) {
            sse_dir.mkdirs();
        }
        File saveFile = Paths.get(sse_pathname, "now.json").toFile();
        if (false) {
            sse_parser_json(saveFile.getAbsolutePath());
            return true;
        }
        String data = "";
        try {
            URL url = new URL(sse_url);
            URLConnection uc = url.openConnection();
            InputStream is = uc.getInputStream();
            InputStreamReader read = new InputStreamReader(is, "GB2312");
            BufferedReader reader = new BufferedReader(read);
            String line;
            while ((line = reader.readLine()) != null) {
                data += line;
                //if (needSaveDownloadHtmlData) {
                //    out.write(line.getBytes());
                //}
            }
            read.close();
            is.close();
        } catch (IOException e) {
            Global.loge(e.getMessage());
        }
        if (data.length() == 0) {
            Global.loge("Error: get sse.com data fail!");
            return false;
        }
        String date = sse_converJson2SQL(data);
        if (date.length()==0 || needSaveDownloadHtmlData) {
            String currFile = Paths.get(sse_pathname, "sse_" + date + ".json").toString();
            try {
                FileOutputStream out = new FileOutputStream(currFile);
                out.write(data.getBytes());
                //Filex.copyFile(saveFile.getAbsolutePath(), currFile);
            } catch (IOException e) {
                Global.loge(e.getMessage());
            }
        }
        
        return true;
    }

    public String sse_converJson2SQL(String jsonStr) {
        try {
            Global.logi(jsonStr);
            JSONObject jsonObject = JSONObject.fromObject(jsonStr);
            String date = jsonObject.get("date").toString();
            String time = jsonObject.get("time").toString();
            String total = jsonObject.get("total").toString();
            JSONArray list = jsonObject.getJSONArray("list");
            Global.logi(date + "," + time + "," + total);
            int count = 0;
            String sql = "";
            for (int i = 0; i < list.size(); i++) {
                JSONArray line = list.getJSONArray(i);
                String code = line.get(0).toString();
                String name = line.get(1).toString();
                double begin = Double.parseDouble(line.get(2).toString());
                double high = Double.parseDouble(line.get(3).toString());
                double low = Double.parseDouble(line.get(4).toString());
                double price = Double.parseDouble(line.get(5).toString());
                double last = Double.parseDouble(line.get(6).toString());
                double percent = Double.parseDouble(line.get(7).toString());
                double volume = Double.parseDouble(line.get(8).toString());
                double money = Double.parseDouble(line.get(9).toString());
                String note =line.get(10).toString();
                double gap = Double.parseDouble(line.get(11).toString());
                double wave = Double.parseDouble(line.get(12).toString());
                String sql_head = "insert into in_sse_temp (date,code,name,begin,low,high,price,volume,money,last,percent,note,gap,wave,data_from,create_time)values ";
                sql = "(";
                sql += "\"" + date + "\",";
                sql += "\"" + code + "\",";
                sql += "\"" + name + "\",";
                sql += begin + ",";
                sql += high + ",";
                sql += price + ",";
                sql += low + ",";
                sql += volume + ",";
                sql += money + ",";
                sql += last + ",";
                sql += percent + ",";
                sql += "\"" + note + "\",";
                sql += gap + ",";
                sql += wave + ",";
                sql += "NULL,";
                sql += "NULL";
                sql += ")";
                if (Global.jdbc.update(sql_head + sql) != -1) {
                    count++;
                }
                Global.logi(date + "," + code + "," + name + "," + begin);
            }
            Global.logi( "count ====" + count+", "+list.size());
            if (count == list.size()) {
                return date;
            }

        } catch (Exception e) {
            Global.loge(e.getMessage());
            return "";
        }
        return "";
    }

    public int sse_parser_json(String filepathname) {
        try {
            // read file content from file
            if (filepathname.length() == 0) {
                return 0;
            }
            File f = new File(filepathname);
            String data = "";
            if (f.isFile() && f.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(f), "gbk");
                BufferedReader reader = new BufferedReader(read);
                String line;
                while ((line = reader.readLine()) != null) {
                    data += line;
                }
                read.close();
            } else {
                return 0;
            }
            sse_converJson2SQL(data);
            return 1;
        } catch (Exception e) {
            Global.loge(e.getMessage());
            return 1;
        }
    }

    private boolean sina_parse_html_table(String htmldata, String fileName) {
        try {
            Document document = Jsoup.parse(htmldata);
            //System.out.println(document);                     
            Element elements = document.getElementById("FundHoldSharesTable");
            //System.out.println(elements);  
            if (elements == null) {
                return false;
            }
            Elements table = elements.select("table");
            if (table == null) {
                return false;
            }
            Elements trs = table.select("tr");
            Global.logi(trs);
            if (trs == null) {
                return false;
            }
            String name = "";
            String code = "";
            if (trs.size() > 0) {
                //Elements thds = trs.get(0).select("thead");
                //System.out.println(thds);  
                Elements ths = trs.get(0).select("th");
                if (ths == null) {
                    return false;
                }
                Global.logi(ths);
                String txt = ths.get(0).text().trim().trim();
                int begin = txt.indexOf("(");
                int end = txt.indexOf(")");
                name = txt.substring(0, begin).trim();
                code = txt.substring(begin + 1, end).trim();
                Global.logd("**," + name + "," + code);
            }
            int i;

            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fileName), "gbk");
            for (i = 2; i < trs.size(); i++) {
                String line = "";
                String date = "";
                String begin = "", high = "", price = "", low = "", valume = "", money = "";
                Elements tds = trs.get(i).select("td");
                for (int j = 0; j < tds.size(); j++) {
                    String txt = tds.get(j).text().trim();
                    if (j == 0) {
                        date = txt;
                    } else if (j == 1) {
                        begin = txt;
                    } else if (j == 2) {
                        high = txt;
                    } else if (j == 3) {
                        price = txt;
                    } else if (j == 4) {
                        low = txt;
                    } else if (j == 5) {
                        valume = txt;
                    } else if (j == 6) {
                        money = txt;
                    }

                    //Global.logi(txt + " ");
                }
                line = "{";
                line += "\"code\":\"" + code + "\",";
                line += "\"name\":\"" + name + "\",";
                line += "\"date\":\"" + date + "\",";
                line += "\"begin\":" + begin + ",";
                line += "\"high\":" + high + ",";
                line += "\"price\":" + price + ",";
                line += "\"low\":" + low + ",";
                line += "\"valume\":" + valume + ",";
                line += "\"money\":" + money + "";
                line += "}";

                Global.logi(line + "");
                out.write(line + "\n");

            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sina_download_parse_htm() {
        String url = "";
        try {
            Document document = Jsoup.connect(url).get();
            if (false) {
                document = Jsoup.connect(url)
                        .data("query", "Java")
                        .userAgent("Mozilla")
                        .cookie("auth", "token")
                        .timeout(3000)
                        .post();
            }
            Element elements = document.getElementById("FundHoldSharesTable");
            //System.out.println(elements);  
            if (elements == null) {
                return false;
            }
            Elements table = elements.select("table");
            if (table == null) {
                return false;
            }
            Elements trs = table.select("tr");
            Global.logi(trs);
            if (trs == null) {
                return false;
            }
            String name = "";
            String code = "";
            if (trs.size() > 0) {
                //Elements thds = trs.get(0).select("thead");
                //System.out.println(thds);  
                Elements ths = trs.get(0).select("th");
                if (ths == null) {
                    return false;
                }
                Global.logi(ths);
                String txt = ths.get(0).text().trim().trim();
                int begin = txt.indexOf("(");
                int end = txt.indexOf(")");
                name = txt.substring(0, begin).trim();
                code = txt.substring(begin + 1, end).trim();
                Global.logi("**," + name + "," + code);
            }
            int i;
            OutputStreamWriter out = null;
            ResultSet rs = Global.jdbc.query("select code,date from in_sina_temp");
            boolean save2File = false;
            if (rs == null) {
                save2File = true;
            }
            if (needSaveDownloadHtmlData) {
                save2File = true;
            }
            if (save2File) {
                String fileName = "";
                out = new OutputStreamWriter(new FileOutputStream(fileName), "GB2312");
            }
            for (i = 2; i < trs.size(); i++) {
                String line = "";
                String date = "";
                String begin = "", high = "", price = "", low = "", valume = "", money = "";
                Elements tds = trs.get(i).select("td");
                for (int j = 0; j < tds.size(); j++) {
                    String txt = tds.get(j).text().trim();
                    if (j == 0) {
                        date = txt;
                    } else if (j == 1) {
                        begin = txt;
                    } else if (j == 2) {
                        high = txt;
                    } else if (j == 3) {
                        price = txt;
                    } else if (j == 4) {
                        low = txt;
                    } else if (j == 5) {
                        valume = txt;
                    } else if (j == 6) {
                        money = txt;
                    }

                    //Global.logi(txt + " ");
                }
                line = "{";
                line += "\"code\":\"" + code + "\",";
                line += "\"name\":\"" + name + "\",";
                line += "\"date\":\"" + date + "\",";
                line += "\"begin\":" + begin + ",";
                line += "\"high\":" + high + ",";
                line += "\"price\":" + price + ",";
                line += "\"low\":" + low + ",";
                line += "\"valume\":" + valume + ",";
                line += "\"money\":" + money + "";
                line += "}";

                Global.logi(line + "");
                String sqlstr = sina_converJson2SQL(line);
                Global.logd("=" + sqlstr);
                if (sqlstr.length() > 0) {
                    int ret = insert_to_table(sqlstr);
                    if (ret == -1) {
                        Global.loge("Error: error json data: " + line);
                    }
                } else {
                    Global.loge("Error: error json data: " + line);
                }
                if (save2File) {
                    out.write(line + "\n");
                }
            }
            if (out != null) {
                out.close();
            }
            return false;

        } catch (IOException e) {
            return false;
        }
    }

    public int parser_sina_data(String filepathname) {
        try {
            // read file content from file
            if (filepathname.length() == 0) {
                return 0;
            }
            File f = new File(filepathname);
            String data = "";
            if (f.isFile() && f.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(f), "gbk");
                BufferedReader reader = new BufferedReader(read);
                String line;
                while ((line = reader.readLine()) != null) {
                    data += line;
                }
                read.close();
            } else {
                return 0;
            }
            File dir = new File(sina_dir);
            String out_dir = Paths.get(sina_dir, "../" + dir.getName() + "out").toString();
            //System.out.println("fileName: " + out_dir);
            dir = new File(out_dir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!dir.exists()) {
                Global.loge("Error: create dir fail! [" + sina_dir + "]");
                return 0;
            }
            String fileName = Paths.get(out_dir, f.getName().replace(".htm", ".data")).toString();
            Global.logd(fileName);
            if (sina_parse_html_table(data, fileName)) {
                return read_sina_to_database(fileName);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }

    private void parseSina(String sina_dir) {
        Global.logd("--" + sina_dir);
        File f = new File(sina_dir);
        if (f != null && f.isDirectory()) {
            String our_dir = Paths.get(sina_dir, "../" + f.getName() + "out").toString();
            File dir = new File(our_dir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!dir.exists()) {
                System.out.println("Error: create dir fail! [" + sina_dir + "]");
                return;
            }
            String outFileName = Paths.get(our_dir, "sina.log").toString();

            try {
                OutputStreamWriter outFile = new OutputStreamWriter(new FileOutputStream(outFileName), "gbk");
                File[] files = f.listFiles();
                Global.logi("--1");
                List fileList = Arrays.asList(files);
                /*
            Collections.sort(fileList, new Comparator<File>() {

                public int compare(File o1, File o2) {
                    if (o1.isDirectory() && o2.isFile()) {
                        return -1;
                    }
                    if (o1.isFile() && o2.isDirectory()) {
                        return 1;
                    }
                    return o1.getName().compareTo(o2.getName());
                }
            });
                 */
                if (files != null) {

                    for (int i = 0; i < files.length; i++) {
                        f = files[i];
                        String filename = f.getName();

                        if (f.isFile()
                                && (filename.indexOf("sina-") == 0)
                                && (filename.indexOf(".htm") == filename.length() - 4)) {
                            Global.logd(i + ":" + f.getAbsolutePath());
                            //String fileName = parser_html_from_sina(f.getAbsolutePath());
                            int ret = parser_sina_data(f.getAbsolutePath());
                            String line = f.getAbsolutePath();
                            if (ret == 0) {
                                line += "   [fail]";
                            }
                            outFile.write(line + "\n");
                        }
                        //if (i > 1) {
                        //break;
                        //}
                    }
                }

                outFile.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String sina_converJson2SQL(String jsonStr) {
        String sql = "";
        try {
            JSONObject jsonObject = JSONObject.fromObject(jsonStr);
            String code = jsonObject.get("code").toString();
            String name = jsonObject.get("name").toString();
            String date = jsonObject.get("date").toString();
            double begin = Double.parseDouble(jsonObject.get("begin").toString());
            double high = Double.parseDouble(jsonObject.get("high").toString());
            double price = Double.parseDouble(jsonObject.get("price").toString());
            double low = Double.parseDouble(jsonObject.get("low").toString());
            long valume = Long.parseLong(jsonObject.get("valume").toString());
            long money = Long.parseLong(jsonObject.get("money").toString());
            sql = "insert into in_sina_temp (date,code,name,begin,low,high,price,volume,money,data_from,create_time)values ";
            sql += "(";
            sql += "\"" + date + "\",";
            sql += "\"" + code + "\",";
            sql += "\"" + name + "\",";
            sql += begin + ",";
            sql += high + ",";
            sql += price + ",";
            sql += low + ",";
            sql += valume + ",";
            sql += money + ",";
            sql += "NULL,";
            sql += "NULL";
            sql += ")";
            return sql;
        } catch (Exception e) {
            Global.loge(e.getMessage());
            return "";
        }
    }

    private int insert_to_table(String sql) {
        return Global.jdbc.update(sql);
    }

    private int read_sina_to_database(String filepathname) {
        try {
            // read file content from file
            if (filepathname.length() == 0) {
                return 0;
            }
            File f = new File(filepathname);
            ArrayList<String> list = new ArrayList<String>();
            if (f.isFile() && f.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(f), "gbk");
                BufferedReader reader = new BufferedReader(read);
                String line;
                while ((line = reader.readLine()) != null) {
                    list.add(line);
                    Global.logd("***" + line);
                }
                read.close();
            } else {
                return 0;
            }
            for (int i = 0; i < list.size(); i++) {
                String line = list.get(i);
                Global.logd("+" + line);
                String sqlstr = sina_converJson2SQL(line);
                Global.logd("=" + sqlstr);
                if (sqlstr.length() > 0) {
                    int ret = insert_to_table(sqlstr);
                    if (ret == -1) {
                        Global.loge("Error: error json data: " + line);
                    }
                } else {
                    Global.loge("Error: error json data: " + line);
                }
            }
            return list.size();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //资源所在的网页地址  
    public void jsoup_demo() {
        String resourceURL = "http://www.csdn.net/";
        //资源下载之后，保存在本地的文件路径  
        String downloadFilePath = "E://downloadImage//";
        try {
            //从一个网站获取和解析一个HTML文档，jsoup的API中有此方法的说明  
            Document document = Jsoup.connect(resourceURL).get();
            //System.out.println(document);  
            //获取所有的img标签  
            Elements elements = document.getElementsByTag("img");
            for (Element element : elements) {
                //获取每个img标签的src属性的内容，即图片地址，加"abs:"表示绝对路径  
                String imgSrc = element.attr("abs:src");
                //下载图片文件到电脑的本地硬盘上  
                System.out.println("正在下载图片：-----------" + imgSrc);

                System.out.println("图片下载完毕：-----------" + imgSrc);
                System.out.println("-------------------------------------------------------------------------------------------------------------");
            }
            System.out.println("共下载了 " + elements.size() + " 个文件(不去重)");
        } catch (IOException e) {
        }
    }
}
