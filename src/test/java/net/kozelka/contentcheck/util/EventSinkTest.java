package net.kozelka.contentcheck.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EventSinkTest {

    private EventSink<MyEvents> myEvents;
    private StringBuilder sb;

    @Before
    public void setup() {
        sb = new StringBuilder();
        myEvents = EventSink.create(MyEvents.class);
        myEvents.addListener(new MyEvents() {
            public void onString(String s) {
                sb.append("<");
                sb.append(s);
                sb.append(">");
            }

            public void onEmpty() {
                sb.append("()");
            }
        });

        myEvents.addListener(new MyEvents() {
            public void onString(String s) {
                sb.append("[");
                sb.append(s);
                sb.append("]");
            }

            public void onEmpty() {
                sb.append("{}");
            }
        });
        myEvents.removeListener(null);
    }

    @Test
    public void testListener() throws Exception {
        myEvents.fire.onString("A");
        myEvents.fire.onEmpty();
        myEvents.fire.onString("bb");
        myEvents.fire.onEmpty();
        myEvents.fire.onString("C");
        Assert.assertEquals("<A>[A](){}<bb>[bb](){}<C>[C]", sb.toString());
    }

    private static interface MyEvents {
        void onString(String s);
        void onEmpty();
    }

}
