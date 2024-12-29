import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.Cleaner.Cleanable;
import java.util.List;

import io.github.seal139.jSwarm.core.NativeCleaner;
import io.github.seal139.jSwarm.core.NativeCleaner.NativeResources;
import io.github.seal139.jSwarm.core.Swarm;
import io.github.seal139.jSwarm.misc.Decompiler;

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
	
	static class Boobo implements NativeResources{
		
		private static final class Del implements Deallocator {
			int addr = 0;
			
			@Override
			public void clean() {
				System.out.println("-->>" + addr);
			}
		}
		
		
		private final Del dealloc = new Del();
		
		Cleanable c;
		
		Boobo(){
			c = NativeCleaner.register(this);
		}
		
		void kaka() {
			dealloc.addr = hashCode();
			c.clean();
			c.clean();
		}
		
		@Override
		public void close() throws Exception {
			// TODO Auto-generated method stub
		}

		@Override
		public Deallocator getDeallocator() {
			return dealloc;
		}

		
	}
	
		
	public static void main (String ...strings ) throws Exception {
		for(int i = 0; i < 10000; i++) {
			Boobo b = new Boobo();
			b.kaka();
		}
		
		BufferedReader reader = new BufferedReader(
	            new InputStreamReader(System.in));

	        // Reading data using readLine
	        String name = reader.readLine();
	        
		 try {
	            Thread.sleep(1000); // Wait a second
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
		
		System.out.println(Swarm.isDebugMode());
		
		Test t = new Test();
		
		t.decompile();
	}
	
	public void decompile() throws Exception {
		String classPath = "/" + ExampleKernel.class.getName().replace('.', '/') + ".class";
		
		
		Decompiler d = Decompiler.getDefault();
		
		String s1 = d.process(List.of(ExampleKernel.class), this::processCuda);

		
		System.out.println();
		
		System.out.println(s1);
		System.out.println("\n\n\n============\n\n\n");
		
        
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
