package com.kntrel.mc.commvoker.bukkit.requirement;

import com.kntrel.mc.commvoker.requirement.Requires;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Requires(PermissionRequirement.class)
public @interface RequiresPermission {

    String[] value();
}
