package test;

import com.github.prchen.antares.starter.AntaresContext;
import test.annotations.Bar;
import test.annotations.Foo;
import test.classes.BarCandidate1;
import test.classes.BarCandidate2;
import test.interfaces.FooInterface;
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
