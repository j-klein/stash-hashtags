package ut.com.jklein.stash.hashtags;

import org.junit.Test;
import com.jklein.stash.hashtags.MyPluginComponent;
import com.jklein.stash.hashtags.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}