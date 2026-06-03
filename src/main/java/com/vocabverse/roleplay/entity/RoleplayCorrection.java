package com.vocabverse.roleplay.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleplayCorrection {

    private String original;
    private String corrected;
    private String betterExpression;
    private String explanation;
}
