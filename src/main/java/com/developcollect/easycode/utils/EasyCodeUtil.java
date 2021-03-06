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
     * ??????????????????????????????????????????????????????classpath
     * ?????????maven??????
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
            // todo ????????????
        });

        invoker.setOutputHandler(new InvocationOutputHandler() {
            @Override
            public void consumeLine(String s) throws IOException {
                // ????????????????????????sb???????????????????????????????????????
                sb.append(s);
            }
        });

        try{
            int exitCode = invoker.execute(request).getExitCode();
            if (exitCode != 0) {
                // ??????maven????????????
                log.error("??????maven????????????");
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
            log.error("??????maven????????????", e);
        }
        return null;
    }

    public static boolean genQueryDslCode(Set<File> sourceDirs, String cps, String outputDir) {
        // projectRootPath ?????????????????????????????????
        Set<File> sourceDirectories = sourceDirs;

        FileUtil.mkdir(outputDir);

        StandardJavaFileManager fileManager = null;

        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                log.warn("You need to run build with JDK or have tools.jar on the classpath.");
                throw new IllegalStateException("?????????JDK");
            }

            Set<File> files = filterFiles(sourceDirectories);

            if (files.isEmpty()) {
                log.info("No Java sources found (skipping)");
                return false;
            }

            fileManager = compiler.getStandardFileManager(null, null, null);
            Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(files);

            // todo  ???????????????clear???????????????????????????????????????????????????????????????Q??????????????????common????????????
            //       common????????????clear?????????????????????common/target/classes?????????????????????clear??????????????????
            //       ??????????????????????????????????????????compileClassPath??????????????????????????????????????????????????????
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
                        if (message.contains("?????????") || message.contains("io.swagger.annotations")) {
                            rv = true;
                            break;
                        }
                        if (message.contains("???????????????") && qPattern.matcher(diagnostic.getSource().getName()).find()) {
                            rv = true;
                            break;
                        }
                    }
                }

                if (Boolean.FALSE.equals(rv) ) {
                    log.debug("??????Q????????????");
                    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnosticCollector.getDiagnostics()) {
                        log.debug("{}", diagnostic);
                    }
                } else {
                    log.debug("??????Q????????????");
                }

                return rv;
            } finally {
                executor.shutdown();
                if (tempDirectory != null) {
                    FileUtils.deleteDirectory(tempDirectory);
                }
            }

        } catch (Exception e1) {
            log.warn("??????Q????????????", e1);
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
        // projectRootPath ?????????????????????????????????
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

        // ???????????????
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




    // ???????????????

    public static void genProjectCode(
            Template template,
            String jdbcUrl, String dbUsername, String dbPassword,
            String projectPath, String subProjectPath, String moduleName, String parentPackage,
            Class idType, boolean isLogicDel, String tableName, String tablePrefix,
            String author, boolean fileOverride, boolean swagger2, boolean lombok
    ) {
        // ????????????
        GlobalConfig gc = new GlobalConfig();
        gc.setOutputDir(projectPath + "/" + subProjectPath + "/src/main/java");
        gc.setAuthor(author);
        gc.setSwagger2(swagger2);
        //??????????????????????????????????????????????????????????????????true????????????
        gc.setFileOverride(fileOverride);
        gc.setOpen(false);

        // ???????????????
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(dressDbUrl(jdbcUrl));
        dsc.setDriverName(getDbDriverName(jdbcUrl));
        dsc.setUsername(dbUsername);
        dsc.setPassword(dbPassword);


        // ?????????
        PackageConfig pc = new PackageConfig();
        pc.setModuleName(moduleName);
        pc.setParent(parentPackage);


        // ???????????????, ??????????????????????????????????????????????????????????????????
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                HashMap<String, Object> map = Maps.newHashMap();
                if (!idType.getName().startsWith("java.lang")) {
                    map.put("idImportName", idType.getName());
                }
                map.put("idType", idType.getSimpleName());

                // ???????????????id
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


        // ????????????????????????
        List<FileOutConfig> focList = new ArrayList<>();
        cfg.setFileOutConfigList(focList);
        for (File templateFile : template.getTemplateFiles()) {
            FileOutConfig fileOutConfig = new TemplateFileOutConfig(template, templateFile, pc, projectPath, subProjectPath);
            focList.add(fileOutConfig);
        }

        // ????????????
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

        // ????????????, ??????????????????????????????????????????
        TemplateConfig templateConfig = new TemplateConfig()
                .setEntity(null)
                .setEntityKt(null)
                .setMapper(null)
                .setService(null)
                .setServiceImpl(null)
                .setXml(null)
                .setController(null);


        // ???????????????
        AutoGenerator mpg = new AutoGenerator() {
            @Override
            protected ConfigBuilder pretreatmentConfigBuilder(ConfigBuilder config) {

                /*
                 * ?????????????????????
                 */
                if (null != injectionConfig) {
                    injectionConfig.initMap();
                    config.setInjectionConfig(injectionConfig);
                }
                /*
                 * ???????????????
                 */
                List<TableInfo> tableList = this.getAllTableInfoList(config);
                for (TableInfo tableInfo : tableList) {
                    /* ---------- ??????????????? ---------- */
                    if (config.getGlobalConfig().isActiveRecord()) {
                        // ?????? ActiveRecord ??????
                        tableInfo.setImportPackages(Model.class.getCanonicalName());
                    }
//                    if (tableInfo.isConvert()) {
//                        // ?????????
//                        tableInfo.setImportPackages(TableName.class.getCanonicalName());
//                    }
//                    if (config.getStrategyConfig().getLogicDeleteFieldName() != null && tableInfo.isLogicDelete(config.getStrategyConfig().getLogicDeleteFieldName())) {
//                        // ??????????????????
//                        tableInfo.setImportPackages(TableLogic.class.getCanonicalName());
//                    }
//                    if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotEmpty(config.getStrategyConfig().getVersionFieldName())) {
//                        // ???????????????
//                        tableInfo.setImportPackages(Version.class.getCanonicalName());
//                    }
                    boolean importSerializable = true;
//                    if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotEmpty(config.getSuperEntityClass())) {
//                        // ?????????
//                        tableInfo.setImportPackages(config.getSuperEntityClass());
//                        importSerializable = false;
//                    }
                    if (config.getGlobalConfig().isActiveRecord()) {
                        importSerializable = true;
                    }
                    if (importSerializable) {
                        tableInfo.setImportPackages(Serializable.class.getCanonicalName());
                    }
                    // Boolean??????is????????????
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


                // ????????????????????????????????????
                List<TableInfo> tableInfoList = config.getTableInfoList();
                for (TableInfo tableInfo : tableInfoList) {
                    final String comment = tableInfo.getComment();
                    if (comment.endsWith("???")) {
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
        // ?????? freemarker ??????
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
        throw new IllegalArgumentException("?????????????????????: " + url);
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
     * ????????????Q?????????????????????????????????
     * @param projectRootDir
     * @param genQueryDslWorkDir
     * @param offsetPath ??????????????????????????????
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
                // classpath?????????????????????????????????????????????????????????????????????????????????????????????????????????
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
                    // ??????????????????
                    FileUtil.copy(file, targets.get(0), true);
                }
            }
        }
    }


    /**
     * ??????maven??????
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
            // ?????????????????????jar?????????????????????
            for (File targetDir : Optional.ofNullable(repoDir.listFiles()).orElse(new File[0])) {
                // ????????????????????????????????????
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
                        // ??????packaging???pom????????????pom??????
                        pomFile = targetDir;
                    }
                }
            }
            if (jarFile != null) {
                // ??????jar?????????
                File jarSha1File = new File(jarFile.getAbsolutePath() + ".sha1");
                if (FileUtil.exist(jarSha1File)) {
                    String sha1 = FileUtil.readAll(jarSha1File).trim().substring(0, 40);
                    String cSha1 = SecureUtil.sha1(jarFile);
                    if (!sha1.equals(cSha1)) {
                        log.debug("[{}]??????", jarFile.getAbsolutePath());
                        if (lineHandler != null) {
                            lineHandler.handle(StrUtil.format("  ??????[{}]?????????[{}]??????", repoDir.getAbsoluteFile(), jarFile.getAbsoluteFile()));
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
                                // pom??????, ????????????jar??????
                                return;
                            }
                        }
                    } catch (Exception e) {
                        String all = FileUtil.readAll(pomFile);
                        if (all.contains("<packaging>pom</packaging>")) {
                            // pom??????, ????????????jar??????
                            return;
                        }
                    }
                }

                // ??????jar??????????????????pom????????????????????????
                if (lineHandler != null) {
                    lineHandler.handle(StrUtil.format("  ??????[{}]???????????????jar??????????????????pom??????", repoDir.getAbsoluteFile()));
                }
                FileUtil.del(repoDir);
                log.debug("?????? ==> {}", repoDir.getAbsolutePath());
            }
        } catch (Exception e) {
            if (lineHandler != null) {
                lineHandler.handle(StrUtil.format("??????Maven?????????[{}]??????", repoDir.getAbsoluteFile()));
            }
            log.error("??????Maven?????????[{}]??????", repoDir.getAbsolutePath(), e);
        }
    }


    public static List<Template> scanTemplates() {
        // ???????????????????????????
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
            // ??????PreparedStatement
            for (int i = 0; i < fakeDataTableInfos.size(); i++) {
                StreamProgress progress = progresses.get(i);
                progress.start();
                FakeDataTableInfo fakeDataTableInfo = fakeDataTableInfos.get(i);

                // ???????????????????????????????????????
                checkCircularReference(fakeDataTableInfo);

                // ??????SQL
                String insertSql = buildSql(context, fakeDataTableInfo);
                PreparedStatement preparedStatement = connection.prepareStatement(insertSql);
                preStatMap.put(fakeDataTableInfo.getTableInfo().getName(), preparedStatement);
            }

            // ????????????
            if (batchSize <= 0 && num < 5000) {
                genFakeDataSimple2(context, fakeDataTableInfos, preStatMap, num, progresses, exitFlg);
            } else {
                genFakeDataBatch2(context, connection, fakeDataTableInfos, preStatMap, num, batchSize, progresses, exitFlg);
            }
        }
    }

    /**
     * ???????????????????????????????????????
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

        // todo ????????????????????????


        // ??????
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
            // ????????????
            context.incrementNo();
            fillFakeData(context, fakeDataTableInfos, preStatMap);
            context.clear();


            // ??????SQL
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

            // ????????????????????????????????????
            if (exitFlg.get()) {
                return;
            }
        }
    }

    private static void genFakeDataBatch2(FakeDataContext context, Connection connection, List<FakeDataTableInfo> fakeDataTableInfos, Map<String, PreparedStatement> preStatMap, long num, long batchSize, List<StreamProgress> progresses, Supplier<Boolean> exitFlg) throws SQLException {
        // ??????????????????
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

                // ????????????
                fillFakeData(context, fakeDataTableInfos, preStatMap);

                for (Map.Entry<String, PreparedStatement> entry : preStatMap.entrySet()) {
                    entry.getValue().addBatch();
                }

                context.clear();
            }

            i += cbSize;

            // ??????SQL
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
                log.debug("??????????????????SQL???{}", preparedStatement);

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
     * ??????????????????????????????????????????
     */
    private static void fillFakeData(FakeDataContext context, List<FakeDataTableInfo> fakeDataTableInfos, Map<String, PreparedStatement> preStatMap) throws SQLException {
        int noFillSum = 0;
        List<FakeDataTableInfo> semiTableInfos = new LinkedList<>();
        for (FakeDataTableInfo fakeDataTableInfo : fakeDataTableInfos) {
            // ???????????????????????????????????????????????????????????????????????????????????????
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
                throw new RuntimeException("????????????????????????");
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
                // ????????????????????????
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
        // ??????????????????
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
