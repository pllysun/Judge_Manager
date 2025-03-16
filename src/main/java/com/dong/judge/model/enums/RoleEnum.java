package com.dong.judge.model.enums;

import lombok.Getter;

/**
 * 角色枚举类
 * <p>
 * 定义系统中的五种角色：超级管理员、管理员、会员、用户和游客
 * 角色等级从高到低排列，高级角色拥有低级角色的所有权限
 * </p>
 */
@Getter
public enum RoleEnum {
    
    /**
     * 超级管理员
     */
    SUPER_ADMIN("ROLE_SUPER_ADMIN", "超级管理员", 5),
    
    /**
     * 管理员
     */
    ADMIN("ROLE_ADMIN", "管理员", 4),
    
    /**
     * 会员
     */
    VIP("ROLE_VIP", "会员", 3),
    
    /**
     * 普通用户
     */
    USER("ROLE_USER", "用户", 2),
    
    /**
     * 游客
     */
    GUEST("ROLE_GUEST", "游客", 1);
    
    /**
     * 角色编码
     */
    private final String code;
    
    /**
     * 角色名称
     */
    private final String name;
    
    /**
     * 角色等级，数字越大等级越高
     */
    private final int level;
    
    RoleEnum(String code, String name, int level) {
        this.code = code;
        this.name = name;
        this.level = level;
    }
    
    /**
     * 获取所有角色编码
     * 
     * @return 所有角色编码数组
     */
    public static String[] getAllRoleCodes() {
        RoleEnum[] values = RoleEnum.values();
        String[] codes = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            codes[i] = values[i].getCode();
        }
        return codes;
    }
    
    /**
     * 根据角色等级获取该等级及以下的所有角色编码
     * 
     * @param level 角色等级
     * @return 该等级及以下的所有角色编码数组
     */
    public static String[] getRoleCodesByLevel(int level) {
        RoleEnum[] values = RoleEnum.values();
        int count = 0;
        for (RoleEnum value : values) {
            if (value.getLevel() <= level) {
                count++;
            }
        }
        
        String[] codes = new String[count];
        int index = 0;
        for (RoleEnum value : values) {
            if (value.getLevel() <= level) {
                codes[index++] = value.getCode();
            }
        }
        return codes;
    }
    
    /**
     * 根据角色编码获取角色枚举
     * 
     * @param code 角色编码
     * @return 角色枚举，如果找不到则返回null
     */
    public static RoleEnum getByCode(String code) {
        for (RoleEnum value : RoleEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}