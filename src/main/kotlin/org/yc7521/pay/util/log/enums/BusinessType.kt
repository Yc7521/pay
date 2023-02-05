package org.yc7521.pay.util.log.enums

/**
 * 业务操作类型
 *
 * @author yc
 */
enum class BusinessType {
    /**
     * 其它
     */
    OTHER,

    /**
     * 查询
     */
    QUERY,

    /**
     * 新增
     */
    INSERT,

    /**
     * 修改
     */
    UPDATE,

    /**
     * 删除
     */
    DELETE,

    /**
     * 授权
     */
    GRANT,

    /**
     * 导出
     */
    EXPORT,

    /**
     * 导入
     */
    IMPORT,

    /**
     * 强退
     */
    FORCE,

    /**
     * 生成代码
     */
    GEN_CODE,

    /**
     * 清空数据
     */
    CLEAN
}