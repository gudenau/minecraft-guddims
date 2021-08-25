package net.gudenau.minecraft.dims.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import org.jetbrains.annotations.NotNull;

public final class UnsafeHelper{
    private UnsafeHelper(){}
    
    private static final MethodHandle allocateInstance$LL;
    static{
        try{
            var Unsafe = Class.forName("sun.misc.Unsafe");
            Object theOne = null;
            for(var field : Unsafe.getDeclaredFields()){
                if(field.getType() == Unsafe && Modifier.isStatic(field.getModifiers())){
                    try{
                        field.setAccessible(true);
                        var unsafe = field.get(null);
                        if(unsafe != null){
                            theOne = unsafe;
                            break;
                        }
                    }catch(ReflectiveOperationException ignored){}
                }
            }
            if(theOne == null){
                throw new RuntimeException("Failed to find Unsafe handle");
            }
            
            var lookup = MethodHandles.lookup();
            allocateInstance$LL = lookup.bind(theOne, "allocateInstance", MethodType.methodType(Object.class, Class.class));
        }catch(Throwable e){
            throw new RuntimeException("Failed to setup UnsafeHelper", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> @NotNull T allocateInstance(@NotNull Class<T> type){
        try{
            return (T)(Object)allocateInstance$LL.invokeExact(type);
        }catch(InstantiationException e){
            throw new RuntimeException("Failed to allocate " + type.getModule().getName() + "/" + type.getName(), e);
        }catch(Throwable e){
            throw new RuntimeException("Failed to invoke Unsafe.allocateInstance", e);
        }
    }
}
