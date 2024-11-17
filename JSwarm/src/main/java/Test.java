import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
	
	
	public static void main (String ...strings ) {
		boo();
	       		
		String classPath = "/" + Expl.class.getName().replace('.', '/') + ".class";
		
        
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
	
	
	
	
	
	public static final class Expl {
		
		public static void doAnything(int[] ret, int[] va, int[] vb, int[] vc) {
			ret[0] = va[0] * vb[0] + vc[0];
		}
	}
}
