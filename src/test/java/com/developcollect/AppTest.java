package com.developcollect;

import static org.junit.Assert.assertTrue;

import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.developcollect.core.utils.FileUtil;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.maven.shared.invoker.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }


    @Test
    public void monitorTest() throws Exception {
        // 1. 监控文件变化
        // 2. 变化了就生成Q对象代码

        // 监控目录
        String rootDir = "F:\\temp";
        // 轮询间隔 5 秒
        long interval = TimeUnit.SECONDS.toMillis(1);

        // 创建过滤器
        IOFileFilter directories = FileFilterUtils.and(
                FileFilterUtils.directoryFileFilter(),
                HiddenFileFilter.VISIBLE);

        IOFileFilter files       = FileFilterUtils.and(
                FileFilterUtils.fileFileFilter(),
                FileFilterUtils.suffixFileFilter(".txt")
        );

        IOFileFilter filter = FileFilterUtils.or(directories, files);
        // 使用过滤器
        FileAlterationObserver observer = new FileAlterationObserver(new File(rootDir), filter);
        //不使用过滤器
        //FileAlterationObserver observer = new FileAlterationObserver(new File(rootDir));
        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {

            }
        });
        //创建文件变化监听器
        FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
        // 开始监控
        monitor.start();

        while (true) {

        }
    }


    @Test
    public void testTableInfo() {
        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl("jdbc:mysql://192.168.0.86:3306/jeemarket-user?useUnicode=true&useSSL=false&characterEncoding=utf8&serverTimezone=GMT%2B8");
        // dsc.setSchemaName("public");
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("123456");
        ConfigBuilder configBuilder = new ConfigBuilder(null, dsc, null, null, null);
        List<TableInfo> tableInfoList = configBuilder.getTableInfoList();


        System.out.println(tableInfoList);
    }


    @Test
    public void ttt() {
        List<File> files = FileUtil.loopDirsAndFilesByPattern("F:\\code2\\arch-learn\\arch-learn\\jpa\\archlearn-jpa", "**/jap-common/src/main/java");
        System.out.println(files);
    }


    public void gg() {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( new File( "F:\\code2\\arch-learn\\arch-learn\\jpa\\archlearn-jpa\\pom.xml" ) );
        request.setGoals( Collections.singletonList( "compile" ) );
        request.setDebug(true);

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File("D:/apache-maven-3.5.3"));


        invoker.setLogger(new PrintStreamLogger(System.err,  InvokerLogger.ERROR){

        } );
        invoker.setOutputHandler(new InvocationOutputHandler() {
            @Override
            public void consumeLine(String s) throws IOException {
//                System.out.println("hhhh");
//                System.out.println(s);
                System.out.println(s);
            }
        });

//
//        try
//        {
//            invoker.execute( request );
//        }
//        catch (MavenInvocationException e)
//        {
//            e.printStackTrace();
//        }



        try{
            if(invoker.execute( request ).getExitCode()==0){
                System.out.println("success");
            }else{
                System.err.println("error");
            }
        }catch (MavenInvocationException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void test_ip() {
    }
}
