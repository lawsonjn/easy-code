package ${thisPkg};

import ${package.Entity}.${entity};
import ${superMapperClassPackage};
import org.springframework.stereotype.Repository;


/**
 * <p>
 * ${table.comment!} Repository 接口
 * </p>
 *
 * @author ${author}
 * @version 1.0
 * @date ${date}
 * @copyright 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
@Repository
public interface ${entity}Repository extends ${superMapperClass}<${entity}, ${cfg.idType}> {

}

