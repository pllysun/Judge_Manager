package com.dong.judge.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**
 * 题目难度等级枚举
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DifficultyLevel {
    EASY_1(1, "简单", "Level 1"),
    EASY_2(2, "简单", "Level 2"),
    MEDIUM_3(3, "中等", "Level 3"),
    MEDIUM_4(4, "中等", "Level 4"),
    MEDIUM_5(5, "中等", "Level 5"),
    HARD_6(6, "困难", "Level 6"),
    HARD_7(7, "困难", "Level 7"),
    HARD_8(8, "困难", "Level 8");

    private final int level;
    private final String difficulty;
    private final String description;

    DifficultyLevel(int level, String difficulty, String description) {
        this.level = level;
        this.difficulty = difficulty;
        this.description = description;
    }

    /**
     * 根据等级获取难度枚举
     */
    public static DifficultyLevel getByLevel(int level) {
        for (DifficultyLevel value : values()) {
            if (value.getLevel() == level) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid difficulty level: " + level);
    }

    /**
     * 获取难度的文字描述
     */
    public String getDisplayText() {
        return String.format("%s(%s)", this.difficulty, this.description);
    }
}