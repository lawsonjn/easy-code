package com.developcollect.easycode;

import com.developcollect.easycode.codegen.Template;
import com.developcollect.easycode.utils.EasyCodeUtil;
import org.junit.Test;

import java.util.List;

public class EasyCodeUtilTest {

    @Test
    public void testGetCompilePaths() {
        List<String> compilePaths = EasyCodeUtil.getCompilePaths("F:\\code2\\arch-learn\\arch-learn\\jpa\\archlearn-jpa");
        System.out.println(compilePaths);
    }

    @Test
    public void testGenQueryDslCode() {
        // F:\\code2\\arch-learn\\arch-learn\\jpa\\archlearn-jpa\\jpa-sample1\\src\\main\\java\\com\\jeecms\\jpasample1\\entity
        EasyCodeUtil.genQueryDslCode(
                "F:\\code2\\arch-learn\\arch-learn\\jpa\\archlearn-jpa\\jpa-sample1",
                "F:\\easy-code-test\\ttt",
                "F:\\code2\\arch-learn\\arch-learn\\jpa\\archlearn-jpa"
        );
    }


    @Test
    public void genProCode() {

        String projectPath = "F:\\code2\\arch-learn\\arch-learn\\jpa\\archlearn-jpa";
        String subProjectPath = "jpa-sample1";
        String author = "zak";
        boolean swagger2 = false;
        boolean fileOverride = true;
        String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/jeemsp";
        String dbUsername = "root";
        String dbPassword = "123456";
        String moduleName = "t44";
        String parentPackage = "com.jeecms.jpasample1";
        Class idType = Long.class;
        boolean isLogicDel = false;
        String tableName = "jc_msp_member";
        String tablePrefix = "jc_msp";

        List<Template> templates = EasyCodeUtil.scanTemplates();
        Template template = templates.get(0);
        // 读取模板


    }

    @Test
    public void scanTemplates() {

//        FileUtil.
        // 相对
        List<Template> templates = EasyCodeUtil.scanTemplates();


        System.out.println(templates);
    }

}