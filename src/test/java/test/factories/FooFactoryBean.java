package test.factories;

import com.github.prchen.antares.starter.AntaresFactoryBean;
import org.springframework.lang.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class FooFactoryBean extends AntaresFactoryBean {

    @Nullable
    @Override
    public Object getObject() throws Exception {
        Class<?> type = getObjectType();
        ClassLoader classLoader = type.getClassLoader();
        Class<?>[] interfaces = new Class[] {type};
        Handler handler = new Handler();
        return Proxy.newProxyInstance(classLoader, interfaces, handler);
    }

    private class Handler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("sayHi")) {
                return "hi";
            }
            return null;
        }
    }
}
