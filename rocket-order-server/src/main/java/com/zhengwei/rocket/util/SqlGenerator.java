package com.zhengwei.rocket.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 *
 * 参考博客 https://www.jianshu.com/p/18111f596ee1
 *
 * @Author : 吟雪情枫
 * @Description 根据实体类生成建库语句
 * @Version : v1.0.0
 * @Date : Created in 2020/7/31 10:12
 * @Modified By :
 **/
public class SqlGenerator {

    private static final Logger logger = LoggerFactory.getLogger(SqlGenerator.class);

    /**
     * 用来存储Java等属性类型与sql中属性类型的对照
     * </br>
     * 例如：java.lang.Integer 对应 integer
     */
    public static Map<String, String> map = new HashMap<>();

    public static void main(String[] args) {
        map.put("class java.lang.String", "varchar(255)");
        map.put("class java.math.BigDecimal", "decimal");
        map.put("class java.lang.Integer", "int");
        map.put("class java.lang.Long", "integer unsigned");
        map.put("class java.lang.byte[]", "blob");
        map.put("class java.lang.Boolean", "bit");
        map.put("class java.math.BigInteger", "bigint unsigned");
        map.put("class java.lang.Float", "float");
        map.put("class java.lang.Double", "float");
        map.put("class java.sql.Date", "datetime");
        map.put("class java.sql.Time", "time");
        map.put("class java.time.LocalDateTime", "timestamp");
        map.put("class java.sql.Timestamp", "datetime");
        map.put("class java.util.Date", "datetime");
        map.put("class java.lang.Byte", "tinyint");
        sqlConstruction();
    }

    /**
     * 生成sql建库语句
     */
    private static void sqlConstruction() {
        // todo 实体类所在的package在磁盘上的绝对路径
        String packageName = "D:\\lio\\myGithub\\spring-rocket\\rocket-order-server\\src\\main\\java\\com\\zhengwei\\rocket\\entity";
        // todo 项目中实体类的路径
        String prefix = "com.zhengwei.rocket.entity.";
        // todo 生成sql文件名称
        String filePath = "D:\\rocket-order-server";
        String className = "";

        StringBuffer sqls = new StringBuffer();
        //获取包下的所有类名称
        List<String> list = getAllClasses(packageName);
        for (String str : list) {
            className = prefix + str.substring(0, str.lastIndexOf("."));
            String sql = generateSql(className, filePath);
            sqls.append(sql);
        }
        System.out.println(sqls.toString());
        StringToSql(sqls.toString(), filePath + ".sql");
    }

    /**
     * 根据实体类生成建表语句
     *
     * @param className 全类名
     * @param filePath  磁盘路径
     * @return
     */
    public static String generateSql(String className, String filePath) {
        try {
            Class<?> clz = Class.forName(className);
            className = clz.getSimpleName();
            // 表表名adminUser → tb_admin_user
            className = "tb_" + getStandardFields(className);
            Field[] fields = clz.getDeclaredFields();
            StringBuffer column = new StringBuffer();
            String varchar = " CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,";
            for (Field f : fields) {
                if ("class java.lang.String".equals(f.getType().toString())){
                    column.append(" \n `" + getStandardFields(f.getName()) + "`" + " " + map.get(f.getType().toString())).append(varchar);
                }else {
                    column.append(" \n `" + getStandardFields(f.getName()) + "`" + " " + map.get(f.getType().toString())).append(",");
                }
            }
            StringBuffer sql = new StringBuffer();
            sql.append("\n DROP TABLE IF EXISTS `" + className + "`; ")
                    .append(" \n CREATE TABLE `" + className + "`  (")
                    .append(" \n `id` int(11) NOT NULL AUTO_INCREMENT,")
                    .append(" \n " + column)
                    .append(" \n PRIMARY KEY (`id`) USING BTREE,")
                    .append("\n INDEX `id`(`id`) USING BTREE")
                    .append(" \n ) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci;");
            return sql.toString();
        } catch (ClassNotFoundException e) {
            logger.debug("该类未找到！");
            return null;
        }

    }

    /**
     * 转换为标准等sql字段 例如 adminUser → admin_user
     *
     * @param str 转换为字符串的字段名
     * @return
     */
    public static String getStandardFields(String str) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i != 0 && (c > 'A' && c < 'Z')) {
                sb.append("_");
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 获取包下面等所有实体类名称，类似于获取 XXX.java
     *
     * @param packageName 全类名
     * @return
     */
    public static List<String> getAllClasses(String packageName) {
        List<String> classList = new ArrayList();
        String className = "";
        File f = new File(packageName);
        if (f.exists() && f.isDirectory()) {
            File[] files = f.listFiles();
            // 遍历实体类下面等所有.java文件 获取其类名
            for (File file : files) {
                className = file.getName();
                classList.add(className);
            }
            return classList;
        } else {
            logger.debug("包路径未找到！");
            return null;
        }
    }

    /**
     * 将生成等String字符串 写进sql文件
     *
     * @param str  String字符串
     * @param path sql文件路径路径
     */
    public static void StringToSql(String str, String path) {
        byte[] sourceByte = str.getBytes();
        FileOutputStream os = null;
        if (null != sourceByte) {
            try {
                //文件路径（路径+文件名）
                File file = new File(path);
                //文件不存在则创建文件，先创建目录
                if (!file.exists()) {
                    File dir = new File(file.getParent());
                    dir.mkdirs();
                    file.createNewFile();
                }
                //文件输出流用于将数据写入文件
                os = new FileOutputStream(file);
                os.write(sourceByte);
                os.flush();
                System.out.println("生成成功!!");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 关闭文件输出流
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
