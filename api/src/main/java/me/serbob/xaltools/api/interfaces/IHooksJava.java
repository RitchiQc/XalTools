package me.serbob.xaltools.api.interfaces;

import me.serbob.xaltools.api.permission.PermissionHook;

import java.util.List;
import java.util.Map;

public interface IHooksJava {
    Map<String, Class<? extends PermissionHook>> getPermissionHooks();
}
