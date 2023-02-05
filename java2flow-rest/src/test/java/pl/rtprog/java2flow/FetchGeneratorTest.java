package pl.rtprog.java2flow;

import org.junit.Test;
import pl.rtprog.java2flow.js.JsGenerator;
import pl.rtprog.java2flow.test.UserAPI;

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
                "./types"
        );
        f.register(UserAPI.class);
        f.exportPathFunction();
        f.exportFunctions();
        System.out.println("Types:");
        System.out.println(types.toString());
        System.out.println("API:");
        System.out.println(api);
    }
}