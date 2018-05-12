package com.github.prchen.test.antares.starter;

import com.github.prchen.antares.starter.AntaresContext;
import com.github.prchen.test.antares.starter.annotations.Bar;
import com.github.prchen.test.antares.starter.annotations.Foo;
import com.github.prchen.test.antares.starter.classes.BarCandidate1;
import com.github.prchen.test.antares.starter.classes.BarCandidate2;
import com.github.prchen.test.antares.starter.interfaces.FooInterface;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class TestCase {

    @Autowired
    private AntaresContext manifestHolder;

    @Autowired(required = false)
    private FooInterface fooInterface;

    @Test
    public void testGenerate() {
        Assert.assertEquals(2, manifestHolder.getManifest(Bar.class).size());
        Assert.assertEquals(1, manifestHolder.getManifest(Foo.class).size());
        Assert.assertTrue(manifestHolder.getManifest(Bar.class).contains(BarCandidate1.class));
        Assert.assertTrue(manifestHolder.getManifest(Bar.class).contains(BarCandidate2.class));
        Assert.assertTrue(manifestHolder.getManifest(Foo.class).contains(FooInterface.class));
        Assert.assertNotNull(fooInterface);
        Assert.assertEquals("hi", fooInterface.sayHi());
    }

}
