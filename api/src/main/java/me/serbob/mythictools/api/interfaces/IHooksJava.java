package me.serbob.mythictools.api.interfaces;

import me.serbob.mythictools.api.permission.PermissionHook;

import java.util.List;
import java.util.Map;

public interface IHooksJava {
    Map<String, Class<? extends PermissionHook>> getPermissionHooks();
}
