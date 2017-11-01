package com.example.team05.mplayer;

import com.google.android.exoplayer2.C;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testvalue(){
        System.out.println(C.TYPE_SS);
        System.out.println(C.TYPE_DASH);
        System.out.println(C.TYPE_HLS);
        System.out.println(C.TYPE_OTHER);
    }
    @Test
    public void stringend(){
        String url="hahaha.mpd";
        System.out.println(url.endsWith(".mpd"));
    }

}