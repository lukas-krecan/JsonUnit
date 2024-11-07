package net.javacrumbs.jsonunit.assertj;


import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AssertFactory;
import org.assertj.core.api.InstanceOfAssertFactory;
import java.lang.reflect.Type;

class ConvertingInstanceOfAssertFactory<T, ASSERT extends AbstractAssert<?, ?>> extends InstanceOfAssertFactory<T, ASSERT> {
    public ConvertingInstanceOfAssertFactory(Class<T> type, AssertFactory<T, ASSERT> delegate) {
        super(type, delegate);
    }

    public ConvertingInstanceOfAssertFactory(Class<T> rawClass, Type[] typeArguments, AssertFactory<T, ASSERT> delegate) {
        super(rawClass, typeArguments, delegate);
    }

    @Override
    public ASSERT createAssert(Object actual) {
        return super.createAssert(actual);
    }
}
