package com.developcollect.easycode.utils;

import cn.hutool.core.io.LineHandler;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.developcollect.core.utils.DateUtil;
import com.developcollect.core.utils.FileUtil;
import com.developcollect.core.utils.JarFileUtil;
import com.google.common.collect.Maps;
import com.developcollect.easycode.codegen.CusStrategyConfig;
import com.developcollect.easycode.codegen.FreemarkerTemplateEngine;
import com.developcollect.easycode.codegen.Template;
import com.developcollect.easycode.codegen.TemplateFileOutConfig;
import com.developcollect.easycode.core.fakedata.*;
import com.querydsl.apt.jpa.JPAAnnotationProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.util.DirectoryScanner;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.sql.DataSource;
import javax.tools.*;
import java.io.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.developcollect.easycode.EasyCodeConfig.EASY_CODE_CONFIG;


/**
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2020/10/12 14:17
 */
@Slf4j
public class EasyCodeUtil {

    /**
     * 传入项目根目录，然后项目编译时的依赖classpath
     * 只支持maven项目
     * @param projectRootPath
     * @return
     * @author Zhu Kaixiao
     * @date 2020/10/12 14:17
     */
    public static List<String> getCompilePaths(String projectRootPath) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(projectRootPath + File.separator + "pom.xml" ));
        request.setGoals(Collections.singletonList("compile"));
        request.setDebug(true);
//        request.setMavenOpts("");

        StringBuilder sb = new StringBuilder();

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(EASY_CODE_CONFIG.getQdMavenHome()));


        invoker.setLogger(new PrintStreamLogger(System.err,  InvokerLogger.ERROR){
            // todo 错误输出
        });

        invoker.setOutputHandler(new InvocationOutputHandler() {
            @Override
            public void consumeLine(String s) throws IOException {
                // 先把输出全部存到sb中，后续在提取出需要的数据
                sb.append(s);
            }
        });

        try{
            int exitCode = invoker.execute(request).getExitCode();
            if (exitCode != 0) {
                // 执行maven命令出错
                log.error("执行maven命令出错");
                return null;
            }
            Pattern compile = PatternPool.get("\\[DEBUG]   \\(f\\) compilePath = \\[(.+?)]");
            String s = sb.toString();
            Matcher matcher = compile.matcher(s);
            Set<String> strSet = new HashSet<>();
            while (matcher.find()) {
                String[] split = matcher.group(1).split(", ");
                strSet.addAll(Arrays.asList(split));
            }
            return new ArrayList<>(strSet);
        }catch (MavenInvocationException e) {
            log.error("执行maven命令出错", e);
        }
        return null;
    }

    public static boolean genQueryDslCode(Set<File> sourceDirs, String cps, String outputDir) {
        // projectRootPath 项目根目录下还有子模块
        Set<File> sourceDirectories = sourceDirs;

        FileUtil.mkdir(outputDir);

        StandardJavaFileManager fileManager = null;

        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                log.warn("You need to run build with JDK or have tools.jar on the classpath.");
                throw new IllegalStateException("未找到JDK");
            }

            Set<File> files = filterFiles(sourceDirectories);

            if (files.isEmpty()) {
                log.info("No Java sources found (skipping)");
                return false;
            }

            fileManager = compiler.getStandardFileManager(null, null, null);
            Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(files);

            // todo  如果项目里clear了，那么通过项目里依赖的类会无法找到，比如Q对象的父类在common工程中，
            //       common工程没有clear的话，可以通过common/target/classes找到依赖，但是clear了就找不到了
            //       所以需要在本地存一份，然后在compileClassPath中加入本地的类，这样编译时才不会报错
            String compileClassPath = cps;

            String processor = JPAAnnotationProcessor.class.getName();

            String outputDirectory = outputDir;
            File tempDirectory = null;



            List<String> compilerOptions = buildCompilerOptions(processor, compileClassPath, outputDirectory, sourceDirectories);

            Writer out = new StringWriter();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
                JavaCompiler.CompilationTask task = compiler.getTask(
                        out,
                        fileManager,
                        diagnosticCollector,
                        compilerOptions,
                        null,
                        compilationUnits1
                );
                Future<Boolean> future = executor.submit(task);
                Boolean rv = future.get();
                if (Boolean.FALSE.equals(rv) ) {
                    Pattern qPattern = Pattern.compile("Q[A-Z].*?\\.java$");
                    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnosticCollector.getDiagnostics()) {
                        String message = diagnostic.getMessage(Locale.ENGLISH);
                        if (message.contains("类重复") || message.contains("io.swagger.annotations")) {
                            rv = true;
                            break;
                        }
                        if (message.contains("找不到符号") && qPattern.matcher(diagnostic.getSource().getName()).find()) {
                            rv = true;
                            break;
                        }
                    }
                }

                if (Boolean.FALSE.equals(rv) ) {
                    log.debug("生成Q对象失败");
                    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnosticCollector.getDiagnostics()) {
                        log.debug("{}", diagnostic);
                    }
                } else {
                    log.debug("生成Q对象成功");
                }

                return rv;
            } finally {
                executor.shutdown();
                if (tempDirectory != null) {
                    FileUtils.deleteDirectory(tempDirectory);
                }
            }

        } catch (Exception e1) {
            log.warn("生成Q对象异常", e1);
            return false;
        } finally {
            if (fileManager != null) {
                try {
                    fileManager.close();
                } catch (Exception e) {
                    log.error("Unable to close fileManager", e);
                }
            }
        }
    }

    public static boolean genQueryDslCode(Set<File> sourceDirs, List<String> cps, String outputDir) {
       return genQueryDslCode(sourceDirs, StringUtils.join(cps, ";"), outputDir);
    }

    public static boolean genQueryDslCode(String projectRootPath, String outputDir, String fff) {
        // projectRootPath 项目根目录下还有子模块
        Set<File> sourceDirectories = Collections.singleton(new File(projectRootPath + "/src/main/java"));
        List<String> compilePaths = getCompilePaths(fff);

        return genQueryDslCode(sourceDirectories, compilePaths, outputDir);
    }

    private static List<String> buildCompilerOptions(String processor, String compileClassPath,
                                                     String outputDirectory, Set<File> sourceDirectories) throws IOException {
        Map<String, String> compilerOpts = new LinkedHashMap<String, String>();

        // Default options
        compilerOpts.put("cp", compileClassPath);

        String sourceEncoding = "UTF-8";
        if (sourceEncoding != null) {
            compilerOpts.put("encoding", sourceEncoding);
        }

        compilerOpts.put("proc:only", null);
        compilerOpts.put("processor", processor);

//        if (options != null) {
//            for (Map.Entry<String, String> entry : options.entrySet()) {
//                if (entry.getValue() != null) {
//                    compilerOpts.put("A" + entry.getKey() + "=" + entry.getValue(), null);
//                } else {
//                    compilerOpts.put("A" + entry.getKey() + "=", null);
//                }
//
//            }
//        }

        if (outputDirectory != null) {
            compilerOpts.put("s", outputDirectory);
        }

//        if (!showWarnings) {
//            compilerOpts.put("nowarn", null);
//        }

        StringBuilder builder = new StringBuilder();
        for (File file : sourceDirectories) {
            if (builder.length() > 0) {
                builder.append(";");
            }
            builder.append(file.getCanonicalPath());
        }
        compilerOpts.put("sourcepath", builder.toString());

        // User options override default options
//        if (compilerOptions != null) {
//            compilerOpts.putAll(compilerOptions);
//        }

        List<String> opts = new ArrayList<String>(compilerOpts.size() * 2);

        for (Map.Entry<String, String> compilerOption : compilerOpts.entrySet()) {
            opts.add("-" + compilerOption.getKey());
            String value = compilerOption.getValue();
            if (StringUtils.isNotBlank(value)) {
                opts.add(value);
            }
        }
        return opts;
    }

        // 源码文件夹
    private static Set<File> filterFiles(Set<File> directories) {
        String[] filters = ALL_JAVA_FILES_FILTER;
//        if (includes != null && !includes.isEmpty()) {
//            filters = includes.toArray(new String[includes.size()]);
//            for (int i = 0; i < filters.length; i++) {
//                filters[i] = filters[i].replace('.', '/') + JAVA_FILE_FILTER;
//            }
//        }

        Set<File> files = new HashSet<File>();
        for (File directory : directories) {
            // support for incremental build in m2e context
            if (directory.isFile()) {
                files.add(directory);
                continue;
            }

            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(directory);
            scanner.setIncludes(filters);
            scanner.scan();
            String[] includedFiles = scanner.getIncludedFiles();

            if (includedFiles != null) {
                for (String includedFile : includedFiles) {
                    if (includedFile.startsWith("q\\Q")) {
                        continue;
                    }
                    files.add(new File(scanner.getBasedir(), includedFile));
                }
            }
        }
        return files;
    }

    private static final String JAVA_FILE_FILTER = "/*.java";
    private static final String[] ALL_JAVA_FILES_FILTER = new String[] { "**" + JAVA_FILE_FILTER };




    // 代码生成器

    public static void genProjectCode(
            Template template,
            String jdbcUrl, String dbUsername, String dbPassword,
            String projectPath, String subProjectPath, String moduleName, String parentPackage,
            Class idType, boolean isLogicDel, String tableName, String tablePrefix,
            String author, boolean fileOverride, boolean swagger2, boolean lombok
    ) {
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        gc.setOutputDir(projectPath + "/" + subProjectPath + "/src/main/java");
        gc.setAuthor(author);
        gc.setSwagger2(swagger2);
        //默认不覆盖，如果文件存在，将不会再生成，配置true就是覆盖
        gc.setFileOverride(fileOverride);
        gc.setOpen(false);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(dressDbUrl(jdbcUrl));
        dsc.setDriverName(getDbDriverName(jdbcUrl));
        dsc.setUsername(dbUsername);
        dsc.setPassword(dbPassword);


        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setModuleName(moduleName);
        pc.setParent(parentPackage);


        // 自定义配置, 在准备配置时会调用一次，在输出时又会调用一次
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                HashMap<String, Object> map = Maps.newHashMap();
                if (!idType.getName().startsWith("java.lang")) {
                    map.put("idImportName", idType.getName());
                }
                map.put("idType", idType.getSimpleName());

                // 序列化类的id
                map.put("entityUid", new Random().nextLong());

                List<TableInfo> tableInfoList = this.getConfig().getTableInfoList();
                TableInfo tableInfo = tableInfoList.get(0);
                String serviceName = tableInfo.getServiceName();
                String serviceVar = StrUtil.lowerFirst(serviceName.substring(1));
                map.put("serviceVar", serviceVar);

                String entityName = tableInfo.getEntityName();
                String entityVar = StrUtil.lowerFirst(entityName);
                map.put("entityVar", entityVar);
                this.setMap(map);
            }
        };


        // 设置自定义的输出
        List<FileOutConfig> focList = new ArrayList<>();
        cfg.setFileOutConfigList(focList);
        for (File templateFile : template.getTemplateFiles()) {
            FileOutConfig fileOutConfig = new TemplateFileOutConfig(template, templateFile, pc, projectPath, subProjectPath);
            focList.add(fileOutConfig);
        }

        // 策略配置
        CusStrategyConfig strategy = new CusStrategyConfig();
        strategy.setRestControllerStyle(true);
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setSuperEntityClass(isLogicDel
                ? template.getLogicDelEntity()
                : template.getPhysicalDelEntity());
        strategy.setEntityLombokModel(lombok);
        strategy.setSuperControllerClass(template.getBaseController());
        strategy.setSuperServiceClass(template.getBaseService());
        strategy.setSuperServiceImplClass(template.getBaseServiceImpl());
        strategy.setSuperMapperClass(template.getBaseMapperClass());
        strategy.setSuperMapperImplClass(template.getBaseMapperImplClass());
        strategy.setInclude(tableName);
        strategy.setSuperEntityColumns(template.getExcludeColumns());
        strategy.setControllerMappingHyphenStyle(false);
//        strategy.setEntityTableFieldAnnotationEnable(true);
        strategy.setTablePrefix(tablePrefix);

        // 模板配置, 清除原有配置，使用自定义配置
        TemplateConfig templateConfig = new TemplateConfig()
                .setEntity(null)
                .setEntityKt(null)
                .setMapper(null)
                .setService(null)
                .setServiceImpl(null)
                .setXml(null)
                .setController(null);


        // 代码生成器
        AutoGenerator mpg = new AutoGenerator() {
            @Override
            protected ConfigBuilder pretreatmentConfigBuilder(ConfigBuilder config) {

                /*
                 * 注入自定义配置
                 */
                if (null != injectionConfig) {
                    injectionConfig.initMap();
                    config.setInjectionConfig(injectionConfig);
                }
                /*
                 * 表信息列表
                 */
                List<TableInfo> tableList = this.getAllTableInfoList(config);
                for (TableInfo tableInfo : tableList) {
                    /* ---------- 添加导入包 ---------- */
                    if (config.getGlobalConfig().isActiveRecord()) {
                        // 开启 ActiveRecord 模式
                        tableInfo.setImportPackages(Model.class.getCanonicalName());
                    }
//                    if (tableInfo.isConvert()) {
//                        // 表注解
//                        tableInfo.setImportPackages(TableName.class.getCanonicalName());
//                    }
//                    if (config.getStrategyConfig().getLogicDeleteFieldName() != null && tableInfo.isLogicDelete(config.getStrategyConfig().getLogicDeleteFieldName())) {
//                        // 逻辑删除注解
//                        tableInfo.setImportPackages(TableLogic.class.getCanonicalName());
//                    }
//                    if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotEmpty(config.getStrategyConfig().getVersionFieldName())) {
//                        // 乐观锁注解
//                        tableInfo.setImportPackages(Version.class.getCanonicalName());
//                    }
                    boolean importSerializable = true;
//                    if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotEmpty(config.getSuperEntityClass())) {
//                        // 父实体
//                        tableInfo.setImportPackages(config.getSuperEntityClass());
//                        importSerializable = false;
//                    }
                    if (config.getGlobalConfig().isActiveRecord()) {
                        importSerializable = true;
                    }
                    if (importSerializable) {
                        tableInfo.setImportPackages(Serializable.class.getCanonicalName());
                    }
                    // Boolean类型is前缀处理
                    if (config.getStrategyConfig().isEntityBooleanColumnRemoveIsPrefix()) {
                        tableInfo.getFields().stream().filter(field -> "boolean".equalsIgnoreCase(field.getPropertyType()))
                                .filter(field -> field.getPropertyName().startsWith("is"))
                                .forEach(field -> {
                                    field.setConvert(true);
                                    field.setPropertyName(com.baomidou.mybatisplus.core.toolkit.StringUtils.removePrefixAfterPrefixToLower(field.getPropertyName(), 2));
                                });
                    }
                }


                Map<String, String> pathInfo = config.getPathInfo();
                List<File> emptyDirs = template.getEmptyDirs();
                for (File emptyDir : emptyDirs) {
                    String relaDir = FileUtil.relaPath(template.getDir(), emptyDir);
                    String packagePath = parentPackage.replaceAll("\\.", "/");
                    String p = projectPath + "/" + subProjectPath + "/src/main/java/" + packagePath + "/" + pc.getModuleName() + "/"
                            + relaDir;
                    pathInfo.put(relaDir, p);
                }


                // 去掉表注释最后的一个表字
                List<TableInfo> tableInfoList = config.getTableInfoList();
                for (TableInfo tableInfo : tableInfoList) {
                    final String comment = tableInfo.getComment();
                    if (comment.endsWith("表")) {
                        tableInfo.setComment(comment.substring(0, comment.length() - 1));
                    }
                }

                return config.setTableInfoList(tableList);
            }
        };
        mpg.setGlobalConfig(gc);
        mpg.setDataSource(dsc);
        mpg.setPackageInfo(pc);
        mpg.setCfg(cfg);
        mpg.setTemplate(templateConfig);
        mpg.setStrategy(strategy);
        // 选择 freemarker 引擎
        mpg.setTemplateEngine(new FreemarkerTemplateEngine(template.getDir()));
        mpg.execute();


    }

    public static List<TableInfo> getDbTable(String url, String username, String password) {
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(dressDbUrl(url));
        // dsc.setSchemaName("public");
        dsc.setDriverName(getDbDriverName(url));
        dsc.setUsername(username);
        dsc.setPassword(password);
        ConfigBuilder configBuilder = new ConfigBuilder(null, dsc, null, null, null);
        List<TableInfo> tableInfoList = configBuilder.getTableInfoList();

        return tableInfoList;
    }

    public static String dressDbUrl(String url) {
        if (StringUtils.startsWithIgnoreCase(url, "JDBC:MYSQL") && !url.contains("?")) {
            return url + "?useUnicode=true&useSSL=false&characterEncoding=utf8&serverTimezone=GMT%2B8&rewriteBatchedStatements=true&useServerPrepStmts=false&allowPublicKeyRetrieval=true";
        }
        return url;
    }

    public static String getDbDriverName(String url) {
        if (StringUtils.startsWithIgnoreCase(url, "JDBC:MYSQL")) {
            return "com.mysql.cj.jdbc.Driver";
        }
        throw new IllegalArgumentException("不支持的数据库: " + url);
    }


    public static List<String> analysisSubProject(String projectDir) {

        if (!FileUtil.isDirectory(projectDir)) {
            return Collections.emptyList();
        }
        if (!FileUtil.exist(projectDir, "pom.xml")) {
            return Collections.emptyList();
        }
        File[] ls = FileUtil.ls(projectDir);

        List<String> collect = Arrays.stream(ls)
                .map(f -> analysisSubProject(f.getAbsolutePath()))
                .flatMap(l -> l.stream())
                .collect(Collectors.toList());
        if (collect.isEmpty()) {
            return Collections.singletonList(projectDir);
        }

        return collect.isEmpty()
                ? Collections.singletonList(projectDir)
                : collect;
    }

    public static String getQueryDslCodeOutPath(String projectPath) {
        return EASY_CODE_CONFIG.getClassPathWorkHome() + File.separator + projectPath.replaceAll("[:/\\\\]", "-") + File.separator + EASY_CODE_CONFIG.getGenQueryDslWork();
    }

    public static String getQueryDslCodeClassesPath(String projectPath) {
        return EASY_CODE_CONFIG.getClassPathWorkHome() + File.separator + projectPath.replaceAll("[:/\\\\]", "-") + File.separator + EASY_CODE_CONFIG.getGenQueryDslClasses();
    }


    /**
     * 把生成的Q对象文件同步到项目中去
     * @param projectRootDir
     * @param genQueryDslWorkDir
     * @param offsetPath 相对实体包的偏移位置
     * @author Zhu Kaixiao
     * @date 2020/10/14 15:07
     */
    public static void syncQueryDslFile(String projectRootDir, String genQueryDslWorkDir, String offsetPath) {
        boolean hasOffsetPath = StringUtils.isNotBlank(offsetPath);
        String offsetPackage = null;
        if (hasOffsetPath) {
            offsetPackage = offsetPath.replaceAll("[/\\\\]+", ".");
            if (!offsetPackage.startsWith(".")) {
                offsetPackage = "." + offsetPackage;
            }
        }
        List<File> qFiles = FileUtil.loopFiles(genQueryDslWorkDir);
        Set<File> workQDir = qFiles.stream().map(File::getParentFile).collect(Collectors.toSet());
        for (File qDir : workQDir) {
            String packPath = qDir.getAbsolutePath().split(EASY_CODE_CONFIG.getGenQueryDslWork())[1];
            String entityPackage = packPath.replaceAll("[/\\\\]+", ".");
            if (entityPackage.startsWith(".")) {
                entityPackage = entityPackage.substring(1);
            }
            List<File> targets = FileUtil.loopDirsByPattern(projectRootDir, "**/**/src/main/java" + packPath.replaceAll("\\\\", "/"));

            if (targets.isEmpty()) {
                // classpath中缓存的实体比项目中多（项目中删除），项目中实际上没有该目录的话就跳过
                continue;
            }

            for (File file : qDir.listFiles()) {
                if (hasOffsetPath) {
                    String codeStr = FileUtil.readAll(file);
                    codeStr = codeStr.replaceAll("package (.+?);", "package $1" + offsetPackage + ";\n\nimport " + entityPackage + ".*;");
                    codeStr = codeStr.replaceAll("(.)\\.Q", "$1" + offsetPackage + ".Q");
                    File target = new File(targets.get(0), offsetPath + File.separator + file.getName());
                    FileUtil.writeAll(codeStr, target);
                } else {
                    // 直接复制替换
                    FileUtil.copy(file, targets.get(0), true);
                }
            }
        }
    }


    /**
     * 清理maven仓库
     */
    public static void clearMavenRepository(String repoDir, LineHandler lineHandler) {
        clearMavenRepository(new File(repoDir), lineHandler);
    }

    public static void clearMavenRepository(File repoDir, LineHandler lineHandler) {

        if (repoDir.isFile()) {
            return;
        }
        boolean hasDir = false;
        File pomFile = null;
        File jarFile = null;

        try {
            // 如果路径下没有jar包，就删除目录
            for (File targetDir : Optional.ofNullable(repoDir.listFiles()).orElse(new File[0])) {
                // 隐藏目录或隐藏文件，跳过
                if (targetDir.getName().startsWith(".")) {
                    continue;
                }
                if (lineHandler != null) {
                    lineHandler.handle(targetDir.getAbsolutePath());
                }

                if (targetDir.isDirectory()) {
                    hasDir = true;
                    clearMavenRepository(targetDir, lineHandler);
                }

                if (targetDir.isFile()) {
                    if (targetDir.getName().endsWith(".jar")) {
                        jarFile = targetDir;
                        break;
                    }
                    if (targetDir.getName().endsWith(".pom")) {
                        // 如果packaging为pom的就只有pom文件
                        pomFile = targetDir;
                    }
                }
            }
            if (jarFile != null) {
                // 但是jar损坏了
                File jarSha1File = new File(jarFile.getAbsolutePath() + ".sha1");
                if (FileUtil.exist(jarSha1File)) {
                    String sha1 = FileUtil.readAll(jarSha1File).trim().substring(0, 40);
                    String cSha1 = SecureUtil.sha1(jarFile);
                    if (!sha1.equals(cSha1)) {
                        log.debug("[{}]损坏", jarFile.getAbsolutePath());
                        if (lineHandler != null) {
                            lineHandler.handle(StrUtil.format("  删除[{}]，因为[{}]损坏", repoDir.getAbsoluteFile(), jarFile.getAbsoluteFile()));
                        }
                        FileUtil.del(repoDir);
                        return;
                    }
                }
            }
            if (jarFile == null && !hasDir) {
                if (pomFile != null) {
                    try {
                        Document pom = XmlUtil.readXML(pomFile);
                        NodeList nl = pom.getElementsByTagName("packaging");
                        if (nl.getLength() > 0) {
                            if ("pom".equals(nl.item(0).getTextContent())) {
                                // pom项目, 里面没有jar文件
                                return;
                            }
                        }
                    } catch (Exception e) {
                        String all = FileUtil.readAll(pomFile);
                        if (all.contains("<packaging>pom</packaging>")) {
                            // pom项目, 里面没有jar文件
                            return;
                        }
                    }
                }

                // 没有jar文件，也不是pom项目，删除文件夹
                if (lineHandler != null) {
                    lineHandler.handle(StrUtil.format("  删除[{}]，因为没有jar文件，也不是pom项目", repoDir.getAbsoluteFile()));
                }
                FileUtil.del(repoDir);
                log.debug("删除 ==> {}", repoDir.getAbsolutePath());
            }
        } catch (Exception e) {
            if (lineHandler != null) {
                lineHandler.handle(StrUtil.format("处理Maven文件夹[{}]异常", repoDir.getAbsoluteFile()));
            }
            log.error("处理Maven文件夹[{}]异常", repoDir.getAbsolutePath(), e);
        }
    }


    public static List<Template> scanTemplates() {
        // 通过配置目录扩展的
        File tmpsDir = new File(EASY_CODE_CONFIG.getCodeTemplateHome());

        List<File> dirs = FileUtil.lsDirs(tmpsDir, f -> new File(f, Template.TEMPLATE_SETTING_FILE).exists());
        List<Template> templates = dirs.stream()
                .map(Template::new)
                .collect(Collectors.toList());

        return templates;
    }


    public static void releaseCodeTemplates() throws IOException {
        JarFileUtil.copy("/codetemplates/", EASY_CODE_CONFIG.getHomeDir(), EasyCodeUtil.class);
    }

    public static void genFakeData2(DataSource dataSource, List<FakeDataTableInfo> fakeDataTableInfos, long num, long batchSize, List<StreamProgress> progresses, Supplier<Boolean> exitFlg) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            FakeDataContext context = new FakeDataContext(num);
            Map<String, PreparedStatement> preStatMap = new LinkedHashMap<>(fakeDataTableInfos.size());
            // 创建PreparedStatement
            for (int i = 0; i < fakeDataTableInfos.size(); i++) {
                StreamProgress progress = progresses.get(i);
                progress.start();
                FakeDataTableInfo fakeDataTableInfo = fakeDataTableInfos.get(i);

                // 调增字段顺序，检测循环引用
                checkCircularReference(fakeDataTableInfo);

                // 生成SQL
                String insertSql = buildSql(context, fakeDataTableInfo);
                PreparedStatement preparedStatement = connection.prepareStatement(insertSql);
                preStatMap.put(fakeDataTableInfo.getTableInfo().getName(), preparedStatement);
            }

            // 生成数据
            if (batchSize <= 0 && num < 5000) {
                genFakeDataSimple2(context, fakeDataTableInfos, preStatMap, num, progresses, exitFlg);
            } else {
                genFakeDataBatch2(context, connection, fakeDataTableInfos, preStatMap, num, batchSize, progresses, exitFlg);
            }
        }
    }

    /**
     * 调增字段顺序，检测循环引用
     */
    private static void checkCircularReference(FakeDataTableInfo fakeDataTableInfo) {
        List<FakeDataFieldInfo> fieldInfos = fakeDataTableInfo.getFieldInfos();
        List<FakeDataFieldInfo> referenceOtherFieldInfos = new LinkedList<>();

        for (FakeDataFieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getDbDataFaker() instanceof ReferenceDbDataFaker) {
                referenceOtherFieldInfos.add(fieldInfo);
            }
        }

        if (referenceOtherFieldInfos.isEmpty()) {
            return;
        }

        // todo 判断是否循环依赖


        // 排序
        fieldInfos.sort(Comparator.comparingInt(referenceOtherFieldInfos::indexOf));
    }

    private static String buildSql(FakeDataContext context, FakeDataTableInfo fakeDataTableInfo) {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ");
        StringBuilder valuesBuilder = new StringBuilder(" ( ");
        String tableName = fakeDataTableInfo.getTableInfo().getName();
        sqlBuilder.append(tableName).append(" (");

        List<FakeDataFieldInfo> fieldInfos = fakeDataTableInfo.getFieldInfos();
        for (FakeDataFieldInfo fieldInfo : fieldInfos) {
            fieldInfo.getDbDataFaker().init(context);
            sqlBuilder.append("`").append(fieldInfo.getTableField().getName()).append("`, ");
            valuesBuilder.append("?").append(", ");
        }
        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length()).append(") VALUES ");
        valuesBuilder.delete(valuesBuilder.length() - 2, valuesBuilder.length()).append(" ) ");
        sqlBuilder.append(valuesBuilder);

        return sqlBuilder.toString();
    }


    private static void genFakeDataSimple2(FakeDataContext context, List<FakeDataTableInfo> fakeDataTableInfos, Map<String, PreparedStatement> preStatMap, long num, List<StreamProgress> progresses, Supplier<Boolean> exitFlg) throws SQLException {
        for (long i = 1; i <= num; i++) {
            // 填充数据
            context.incrementNo();
            fillFakeData(context, fakeDataTableInfos, preStatMap);
            context.clear();


            // 执行SQL
            for (int k = 0; k < fakeDataTableInfos.size(); k++) {
                FakeDataTableInfo fakeDataTableInfo = fakeDataTableInfos.get(k);
                PreparedStatement preparedStatement = preStatMap.get(fakeDataTableInfo.getTableInfo().getName());
                preparedStatement.execute();

                StreamProgress progress = progresses.get(k);
                progress.progress(i);

                if (i == num) {
                    progress.finish();
                }
            }

            // 中途点击了停止，直接退出
            if (exitFlg.get()) {
                return;
            }
        }
    }

    private static void genFakeDataBatch2(FakeDataContext context, Connection connection, List<FakeDataTableInfo> fakeDataTableInfos, Map<String, PreparedStatement> preStatMap, long num, long batchSize, List<StreamProgress> progresses, Supplier<Boolean> exitFlg) throws SQLException {
        // 关闭自动提交
        connection.setAutoCommit(false);
        if (batchSize <= 0) {
            batchSize = 100000;
            if (num <= 10000) {
                batchSize = 1000;
            } else if (num <= 100000) {
                batchSize = 10000;
            }
        }

        for (long i = 0; i < num;) {
            long cbSize = Math.min(batchSize, num - i);

            for (long l = 0; l < cbSize; l++) {
                context.incrementNo();

                // 填充数据
                fillFakeData(context, fakeDataTableInfos, preStatMap);

                for (Map.Entry<String, PreparedStatement> entry : preStatMap.entrySet()) {
                    entry.getValue().addBatch();
                }

                context.clear();
            }

            i += cbSize;

            // 执行SQL
            for (int k = 0; k < fakeDataTableInfos.size(); k++) {
                FakeDataTableInfo fakeDataTableInfo = fakeDataTableInfos.get(k);
                PreparedStatement preparedStatement = preStatMap.get(fakeDataTableInfo.getTableInfo().getName());
                preparedStatement.executeBatch();
                connection.commit();

                StreamProgress progress = progresses.get(k);
                progress.progress(i);

                if (i == num) {
                    progress.finish();
                }
            }


            if (exitFlg.get()) {
                return;
            }
        }
    }

    public static void genFakeData(DataSource dataSource, List<FakeDataTableInfo> fakeDataTableInfos, long num, long batchSize, List<StreamProgress> progresses, Supplier<Boolean> exitFlg) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            FakeDataContext context = new FakeDataContext(num);
            for (int i = 0; i < fakeDataTableInfos.size(); i++) {
                StreamProgress progress = progresses.get(i);
                progress.start();
                FakeDataTableInfo fakeDataTableInfo = fakeDataTableInfos.get(i);
                StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ");
                StringBuilder valuesBuilder = new StringBuilder(" ( ");
                String tableName = fakeDataTableInfo.getTableInfo().getName();
                sqlBuilder.append(tableName).append(" (");

                List<FakeDataFieldInfo> fieldInfos = fakeDataTableInfo.getFieldInfos();
                for (FakeDataFieldInfo fieldInfo : fieldInfos) {
                    fieldInfo.getDbDataFaker().init(null);
                    sqlBuilder.append("`").append(fieldInfo.getTableField().getName()).append("`, ");
                    valuesBuilder.append("?").append(", ");
                }
                sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length()).append(") VALUES ");
                valuesBuilder.delete(valuesBuilder.length() - 2, valuesBuilder.length()).append(" ) ");
                sqlBuilder.append(valuesBuilder);

                PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString());
                log.debug("执行数据生成SQL：{}", preparedStatement);

                if (batchSize <= 0 && num < 5000) {
                    genFakeDataSimple(context, fakeDataTableInfo, preparedStatement, num, progress, exitFlg);
                } else {
                    genFakeDataBatch(context, connection, preparedStatement, fakeDataTableInfo, num, batchSize, progress, exitFlg);
                }

                progress.finish();
            }
        }

    }

    /**
     * 填充数据，并处理依赖字段填充
     */
    private static void fillFakeData(FakeDataContext context, List<FakeDataTableInfo> fakeDataTableInfos, Map<String, PreparedStatement> preStatMap) throws SQLException {
        int noFillSum = 0;
        List<FakeDataTableInfo> semiTableInfos = new LinkedList<>();
        for (FakeDataTableInfo fakeDataTableInfo : fakeDataTableInfos) {
            // 因为依赖其他的字段，所以没填充完成，等第一次循环后再次填充
            int noFill = fillFakeData(context, fakeDataTableInfo, preStatMap.get(fakeDataTableInfo.getTableInfo().getName()));
            if (noFill > 0) {
                noFillSum += noFill;
                semiTableInfos.add(fakeDataTableInfo);
            }
        }

        if (noFillSum <= 0) {
            return;
        }

        for (int i = 0;; i++) {
            Iterator<FakeDataTableInfo> iterator = semiTableInfos.iterator();
            while (iterator.hasNext()) {
                FakeDataTableInfo semiTableInfo = iterator.next();
                int noFill = fillFakeData(context, semiTableInfo, preStatMap.get(semiTableInfo.getTableInfo().getName()));
                if (noFill == 0) {
                    iterator.remove();
                }
            }

            if (semiTableInfos.isEmpty()) {
                return;
            }

            if (i > noFillSum) {
                throw new RuntimeException("字段填充循环依赖");
            }
        }

    }

    private static int fillFakeData(FakeDataContext context, FakeDataTableInfo fakeDataTableInfo, PreparedStatement preparedStatement) throws SQLException {
        int noFill = 0;
        List<FakeDataFieldInfo> fieldInfos = fakeDataTableInfo.getFieldInfos();
        for (int k = 0; k < fieldInfos.size(); k++) {
            FakeDataFieldInfo fakeDataFieldInfo = fieldInfos.get(k);
            String tableName = fakeDataTableInfo.getTableInfo().getName();
            String fieldName = fakeDataFieldInfo.getTableField().getName();
            if (context.exists(tableName, fieldName)) {
                continue;
            }
            Object fakeData;
            try {
                fakeData = fakeDataFieldInfo.getDbDataFaker().getFakerData(context);
            } catch (ReferenceNotFoundException e) {
                // 依赖的字段不存在
                ++noFill;
                continue;
            }
            context.addFieldValue(tableName, fieldName, fakeData);
            int pos = k + 1;
            if (fakeData == null) {
                preparedStatement.setObject(pos, null);
            } else if (fakeData instanceof String) {
                preparedStatement.setString(pos, (String) fakeData);
            } else if (fakeData instanceof Integer) {
                preparedStatement.setInt(pos, (Integer) fakeData);
            } else if (fakeData instanceof Long) {
                preparedStatement.setLong(pos, (Long) fakeData);
            } else if (fakeData instanceof Double) {
                preparedStatement.setDouble(pos, (Double) fakeData);
            } else if (fakeData instanceof Float) {
                preparedStatement.setFloat(pos, (Float) fakeData);
            } else if (fakeData instanceof Boolean) {
                preparedStatement.setBoolean(pos, (Boolean) fakeData);
            } else if (fakeData instanceof BigDecimal) {
                preparedStatement.setBigDecimal(pos, (BigDecimal) fakeData);
            } else if (fakeData instanceof Date) {
                preparedStatement.setDate(pos, new java.sql.Date(((Date) fakeData).getTime()));
            } else if (fakeData instanceof LocalDate) {
                long milli = DateUtil.toMilli(LocalDateTime.of((LocalDate) fakeData, LocalTime.MIN));
                preparedStatement.setDate(pos, new java.sql.Date(milli));
            } else if (fakeData instanceof LocalDateTime) {
                long milli = DateUtil.toMilli((LocalDateTime) fakeData);
                preparedStatement.setDate(pos, new java.sql.Date(milli));
            } else if (fakeData instanceof LocalTime) {
                long milli = DateUtil.toMilli(LocalDateTime.of(LocalDate.now(), (LocalTime) fakeData));
                preparedStatement.setTime(pos, new Time(milli));
            }
        }

        return noFill;
    }

    private static void genFakeDataSimple(FakeDataContext context, FakeDataTableInfo fakeDataTableInfo, PreparedStatement preparedStatement, long num, StreamProgress progress, Supplier<Boolean> exitFlg) throws SQLException {
        for (long j = 0; j < num;) {
            fillFakeData(context, fakeDataTableInfo, preparedStatement);
            preparedStatement.execute();
            progress.progress(++j);
            if (exitFlg.get()) {
                return;
            }
        }
    }

    private static void genFakeDataBatch(FakeDataContext context, Connection connection, PreparedStatement preparedStatement, FakeDataTableInfo fakeDataTableInfo, long num, long batchSize, StreamProgress progress, Supplier<Boolean> exitFlg) throws SQLException {
        // 关闭自动提交
        connection.setAutoCommit(false);
        if (batchSize <= 0) {
            batchSize = 100000;
            if (num <= 10000) {
                batchSize = 1000;
            } else if (num <= 100000) {
                batchSize = 10000;
            }
        }

        for (long i = 0; i < num;) {
            long cbSize = Math.min(batchSize, num - i);

            for (long l = 0; l < cbSize; l++) {
                fillFakeData(context, fakeDataTableInfo, preparedStatement);
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
            connection.commit();
            progress.progress(i += cbSize);
            if (exitFlg.get()) {
                return;
            }
        }
    }
}
