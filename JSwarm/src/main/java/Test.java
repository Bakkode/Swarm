import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.List;

import io.github.seal139.jSwarm.core.Decompiler;
import io.github.seal139.jSwarm.runtime.ExampleKernel;
import io.github.seal139.jSwarm.runtime.FooKernel;
import io.github.seal139.jSwarm.wrapper.Swarm;

public class Test {	
	public static void boo() {
		String classPath = "/" + Test.Expl.class.getName().replace('.', '/') + ".class";

        try {
            // Run javap command to disassemble bytecode of the class
            ProcessBuilder pb = new ProcessBuilder("javap", "-c", Test.Expl.class.getName());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read and print the output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            // Wait for the command to finish
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
	
	
	public static void main (String ...strings ) throws Exception {
		System.out.println(Swarm.isDebug());
		
		Test t = new Test();
		
		t.decompile();
	}
	
	public void decompile() throws Exception {
		String classPath = "/" + ExampleKernel.class.getName().replace('.', '/') + ".class";
		
		
		Decompiler d = Decompiler.getDefault();
		
		String s1 = d.process(List.of(ExampleKernel.class), this::processCuda);
		String s2 = d.process(List.of(FooKernel.class), this::processCuda);
		String s3 = d.process(List.of(ExampleKernel.class, FooKernel.class), this::processCuda);

		
		System.out.println();
		
		System.out.println(s1);
		System.out.println("\n\n\n============\n\n\n");
		System.out.println(s2);
		System.out.println("\n\n\n============\n\n\n");
		System.out.println(s3);
		
		
        
        try (InputStream input = Test.class.getResourceAsStream(classPath)) {
            if (input != null) {
                System.out.println("Bytecode for " +  ":");
                byte[] buffer = input.readAllBytes();
                
                // Print bytecode in hexadecimal format
                for (byte b : buffer) {
                    System.out.printf("%02x ", b);
                }
                System.out.println();
            } else {
                System.out.println("Class not found in the JAR.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public String processCuda(String s) {
		return s;
	}
	
	
	
	
	
	public static final class Expl {
		
		public static void doAnything(int[] ret, int[] va, int[] vb, int[] vc) {
			ret[0] = va[0] * vb[0] + vc[0];
		}
	}
}
