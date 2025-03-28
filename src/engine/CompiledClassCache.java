package engine;

import java.util.HashMap;
import java.util.Map;

import types.Clazz;
import types.Method;

/**
 * Singleton cache for compiled code.
 */
public class CompiledClassCache {
    private static CompiledClassCache instance;

    public static CompiledClassCache instance() {
        if (instance == null) {
            instance = new CompiledClassCache();
        }

        return instance;
    }

    private Map<String, Clazz> classes;

    public CompiledClassCache() {
        classes = new HashMap<>();
    }

    /**
     * Creates a cache entry for a clazz.
     * 
     * @param clazz the clazz
     */
    public void saveClass(Clazz clazz) {
        classes.put(clazz.getName(), clazz);
    }

    /**
     * Returns the cache entry for a class.
     * 
     * @param className the class name
     * @return the Clazz
     */
    public Clazz resolveClass(String className) {
        return classes.get(className);
    }

    /**
     * Caches a method definition.
     * 
     * @param className the class name
     * @param method    the method definition
     */
    public void saveMethod(String className, Method method) {
        Clazz clazz = classes.computeIfAbsent(className, Clazz::new);
        clazz.addMethod(method);
    }

    /**
     * Resolves a method definition.
     * 
     * @param className  the class name
     * @param methodName the method name
     * @return the method definition
     */
    public Method resolveMethod(String className, String methodName) {
        Clazz clazz = classes.get(className);
        return clazz.getMethod(methodName);
    }
}
