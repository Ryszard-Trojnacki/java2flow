package pl.rtprog.java2flow;

import org.junit.Test;
import pl.rtprog.java2flow.js.JsGenerator;
import pl.rtprog.java2flow.test.UserAPI;

import static org.junit.Assert.*;

public class FetchGeneratorTest {
    @Test
    public void simpleTest() {
        Java2Flow types=new Java2Flow();
        types.addHeader();
        StringBuilder api=new StringBuilder();
        JsGenerator g=new JsGenerator(api, 0, true, true);
        FetchGenerator f=new FetchGenerator(g, types,
                "appFetch",
                "import appFetch from './network';",
                "import * from './types';"
        );
        f.export(UserAPI.class);
        f.exportFunctions();
        System.out.println("Types:");
        System.out.println(types.toString());
        System.out.println("API:");
        System.out.println(api);
    }
}