package com.developcollect.easycode;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.json.JSONUtil;
import com.developcollect.core.utils.CglibUtil;
import com.developcollect.core.utils.ReflectUtil;
import com.developcollect.easycode.core.db.DbInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2020/10/12 14:21
 */
@Data
@Slf4j
public class EasyCodeConfig {

    private static final String HOME_DIR = "easy-code";
    private static final String CODE_TEMPLATE_HOME_DIR = "codetemplates";
    private static final String CLASSPATH_HOME_DIR = "cpwork";
    private static final String CONFIG_FILE = ".jeecms-easy-code.conf";
    private static final String GEN_QUERYDSL_WORK = "EASY_CODE_WORK";
    private static final String GEN_QUERYDSL_CLASSES = "EASY_CODE_CLASSES";
    public static final EasyCodeConfig EASY_CODE_CONFIG = configProxy();





    /* 基础设置 */
    private Boolean minimizeOnClose = false;

    /* 其他配置 */
    private String otMavenRepo = "";
    private int restReminderInterval = 45;
    private Boolean restReminder = false;
    private String lastRunVersion = "1.50";
//    private boolean 间隔


    /* QueryDSL相关设置 */

    private String qdMavenHome = Optional.ofNullable(System.getenv("MAVEN_HOME")).orElse("");
    private String qdProjectRootDir = "";
    private String qdEntityPackage = "**/jpa";
    private String qdQueryDslCodePath = "q";
    /**
     * classpaths
     */
    private String qdCps = "";


    /* 代码生成相关配置 */

    private Integer genCodeDb = 0;
    private String gcDbUrl = "jdbc:mysql://127.0.0.1:3306/test";
    private String gcDbUsername = "root";
    private String gcDbPassword = "123456";
    private String gcAuthor = "zak";
    private String gcProjectRootDir = "";
    private String gcSubProject = "";
    private String gcModuleName = "";
    private String gcPackage = "";
    private String gcIdType = "Long";
    private String gcTablePrefix = "jc_";
    private Boolean gcFileOverride = false;
    private Boolean gcSwagger2 = false;
    private Boolean gcLombok = false;
    private String gcTable = "";
    private Boolean gcLogicDel = false;
    private String templateName = "jpa";

    /**
     * 生成文件保存位置
     */
    private String productDir = getHomeDir() + "/product";


    /**
     * 加密和编码配置
     */

    private String sm2PrivateKey = "00ED5BC2B0A15FD60828BCDCEE03423A538DDA70E4B3D3754CAD5828689AB6ADDD";
    private String sm2PublicKey = "04AF0FCC45059AA342221352E5268614F2FF7A430497B156C0DEE6E751AB44E4957E9E69299E2CD38E25985B7BD34E0E7BBA683DE4725A6A8CD07E19BFF8BEF44D";


    private List<DbInfo> dbInfos = new ProxyList<>();


    private Integer genFakeDataDb = 0;
    private String genFakeDataTable = "";












    //  事件监听
    private List<PropertyChangeListener> propertyChangeListeners = new ArrayList<>();


    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        propertyChangeListeners.add(propertyChangeListener);
    }

    private void firePropertyChange(String property, Object oldValue, Object newValue) {
        for (PropertyChangeListener propertyChangeListener : propertyChangeListeners) {
            propertyChangeListener.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }



    private static void saveConfig(EasyCodeConfig config) {
        try (BufferedWriter writer = FileUtil.getWriter(getConfFile(), StandardCharsets.UTF_8, false)) {
            Field[] fields = EasyCodeConfig.class.getDeclaredFields();
            Field.setAccessible(fields, true);
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                // 如果不是基本对象，则转json
                if (ClassUtil.isBasicType(field.getType()) || field.getType() == String.class) {
                    writer.write(field.getName() + "=" + field.get(config) + "\n");
                } else {
                    writer.write(field.getName() + "=" + JSONUtil.toJsonStr(field.get(config)) + "\n");
                }
            }
            writer.flush();
        } catch (Exception e) {
            EasyCodeConfig.log.error("保存设置失败", e);
            throw new RuntimeException("保存设置失败");
        }
    }


    private static void readConfig(EasyCodeConfig config) {
        try (BufferedReader reader = FileUtil.getUtf8Reader(getConfFile())) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                int si = line.indexOf("=");
                if (si < 1 || si == line.length() - 1) {
                    continue;
                }
                String key = line.substring(0, si);
                String value = line.substring(si + 1);

                try {
                    Field field = EasyCodeConfig.class.getDeclaredField(key);
                    field.setAccessible(true);
                    if (field.getType() == int.class || field.getType() == Integer.class) {
                        field.set(config, Integer.valueOf(value));
                    } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                        field.set(config, Boolean.valueOf(value));
                    } else if (field.getType() == String.class) {
                        field.set(config, value);
                    } else if (List.class.isAssignableFrom(field.getType())) {
                        Class typeArgument = (Class) TypeUtil.getTypeArgument(field.getGenericType(), 0);
                        List list = JSONUtil.toList(value, typeArgument);
                        field.set(config, new ProxyList(list));
                    }
                } catch (Exception ex) {
                    log.error("读取配置文件失败", ex);
                }
            }
        } catch (Exception e) {
            log.error("读取设置失败", e);
            throw new RuntimeException("读取设置失败");
        }
    }

    private static File getConfFile() {
        File settingFile = new File(FileUtil.getUserHomeDir(), HOME_DIR + File.separator + CONFIG_FILE);
        return FileUtil.touch(settingFile);
    }

    public String getHomeDir() {
        return FileUtil.getUserHomePath() + File.separator + HOME_DIR;
    }

    public String getCodeTemplateHome() {
        return getHomeDir() + File.separator + CODE_TEMPLATE_HOME_DIR;
    }

    public String getClassPathWorkHome() {
        return getHomeDir() + File.separator + CLASSPATH_HOME_DIR;
    }

    public String getGenQueryDslWork() {
        return GEN_QUERYDSL_WORK;
    }

    public String getGenQueryDslClasses() {
        return GEN_QUERYDSL_CLASSES;
    }

    private static EasyCodeConfig configProxy() {
        if (EASY_CODE_CONFIG != null) {
            return EASY_CODE_CONFIG;
        }

        EasyCodeConfig configProxy = CglibUtil.proxy(EasyCodeConfig.class, (target, method, args, methodProxy) -> {
            if (method.getName().startsWith("set")) {
                // 如果设置项没变，就不用保存
                Object oldVal = ReflectUtil.invoke(target, "g" + method.getName().substring(1));
                if (Objects.equals(oldVal, args[0])) {
                    return null;
                }
                Object ret = methodProxy.invokeSuper(target, args);
                EasyCodeConfig.saveConfig((EasyCodeConfig) target);

                // 触发设置项变更事件
                String property = StrUtil.lowerFirst(method.getName().substring(3));
                ((EasyCodeConfig) target).firePropertyChange(property, oldVal, args[0]);
                return ret;
            }
            return methodProxy.invokeSuper(target, args);
        });

        // read是通过反射直接设置字段的值，不会触发代理逻辑
        EasyCodeConfig.readConfig(configProxy);
        return configProxy;
    }


    public static void setValue(String fieldName, Object val) {
        Field field = ReflectUtil.getField(EasyCodeConfig.class, fieldName);
        if (field == null) {
            return;
        }
        Class<?> fieldType = field.getType();
        if (fieldType == int.class) {
            val = Integer.parseInt((String) val);
        }
        ReflectUtil.invoke(EASY_CODE_CONFIG, "set" + StrUtil.upperFirst(fieldName), val);
    }




    static class ProxyList<E> implements List<E> {

        private List<E> list;

        ProxyList() {
            this.list = new ArrayList<>();
        }

        ProxyList(List<E> list) {
            this.list = new ArrayList<>(list);
        }


        @Override
        public int size() {
            return list.size();
        }

        @Override
        public boolean isEmpty() {
            return list.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return list.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return list.iterator();
        }

        @Override
        public Object[] toArray() {
            return list.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return list.toArray(a);
        }

        @Override
        public boolean add(E e) {
            boolean add = list.add(e);
            EasyCodeConfig.saveConfig(EASY_CODE_CONFIG);
            return add;
        }

        @Override
        public boolean remove(Object o) {
            boolean remove = list.remove(o);
            EasyCodeConfig.saveConfig(EASY_CODE_CONFIG);
            return remove;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return list.contains(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            boolean b = list.addAll(c);
            EasyCodeConfig.saveConfig(EASY_CODE_CONFIG);
            return b;
        }

        @Override
        public boolean addAll(int index, Collection<? extends E> c) {
            boolean b = list.addAll(c);
            EasyCodeConfig.saveConfig(EASY_CODE_CONFIG);
            return b;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean b = list.removeAll(c);
            EasyCodeConfig.saveConfig(EASY_CODE_CONFIG);
            return b;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return list.retainAll(c);
        }

        @Override
        public void clear() {
            list.clear();
            EasyCodeConfig.saveConfig(EASY_CODE_CONFIG);
        }

        @Override
        public E get(int index) {
            return list.get(index);
        }

        @Override
        public E set(int index, E element) {
            E set = list.set(index, element);
            EasyCodeConfig.saveConfig(EASY_CODE_CONFIG);
            return set;
        }

        @Override
        public void add(int index, E element) {
            list.add(index, element);
            EasyCodeConfig.saveConfig(EASY_CODE_CONFIG);
        }

        @Override
        public E remove(int index) {
            E remove = list.remove(index);
            EasyCodeConfig.saveConfig(EASY_CODE_CONFIG);
            return remove;
        }

        @Override
        public int indexOf(Object o) {
            return list.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return list.lastIndexOf(o);
        }

        @Override
        public ListIterator<E> listIterator() {
            return list.listIterator();
        }

        @Override
        public ListIterator<E> listIterator(int index) {
            return list.listIterator(index);
        }

        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            return null;
        }
    }



    public interface PropertyChangeListener {

        void propertyChange(PropertyChangeEvent event);

    }



    public static class PropertyChangeEvent extends EventObject {

        private String property;
        private Object oldValue;
        private Object newValue;


        public PropertyChangeEvent(Object source, String property, Object oldValue, Object newValue) {
            super(source);
            this.property = property;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public String getProperty() {
            return property;
        }

        public Object getOldValue() {
            return oldValue;
        }

        public Object getNewValue() {
            return newValue;
        }
    }
}




