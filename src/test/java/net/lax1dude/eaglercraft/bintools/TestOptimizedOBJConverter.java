package net.lax1dude.eaglercraft.bintools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.io.FileInputStream;
import java.io.IOException;

@Testable
public class TestOptimizedOBJConverter {
    @Test
    @SuppressWarnings("ConstantConditions")
    public void test() throws IOException {
        String[] paramz = {
                "samples/obj2mdl-fnaw/laxativedude1.obj", "samples/obj2mdl-fnaw/tests/output/laxativedude1.mdl", "true"
        };
        OBJConverter._main(paramz, true);
        paramz[1] = "samples/obj2mdl-fnaw/tests/output/o_laxativedude1.mdl";
        OptimizedOBJConverter._main(paramz, true);

        try (FileInputStream optimizedProgramOutput = new FileInputStream(paramz[1]);
             FileInputStream originalProgramOutput = new FileInputStream( "samples/obj2mdl-fnaw/tests/output/laxativedude1.mdl")) {

            int o;
            int r;

            while ((o = optimizedProgramOutput.read()) != -2 && (r = originalProgramOutput.read()) != -2) {
                if (o == r && r == -1)
                    break;

                if (o != r)
                    Assertions.fail("Output is not the same!");
            }
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }
}
