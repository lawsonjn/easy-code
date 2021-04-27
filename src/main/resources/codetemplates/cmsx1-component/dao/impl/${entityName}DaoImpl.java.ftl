package ${thisPkg};

import ${package.Entity}.${entity};
import ${superMapperImplClassPackage};
import ${.globals["/dao/ext/$\{entityName}DaoExt.java.ftl"]};


/**
 * <p>
 * ${table.comment!} Dao 实现
 * </p>
 *
 * @author ${author}
 * @version 1.0
 * @date ${date}
 * @copyright 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class ${entity}DaoImpl extends ${superMapperImplClass}<${entity}> implements ${entity}DaoExt {

}

