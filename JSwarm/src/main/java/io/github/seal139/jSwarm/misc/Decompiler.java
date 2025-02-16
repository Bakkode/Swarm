package io.github.seal139.jSwarm.misc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.api.OutputSinkFactory;

import io.github.seal139.jSwarm.core.Program;
import io.github.seal139.jSwarm.core.SwarmException;

/**
 * Decompile Java byte code into generic source
 */
public abstract class Decompiler {

    /**
     * Hide implementation
     */
    private static final class Default extends Decompiler {

        private static final class Cfr implements OutputSinkFactory {

            private final StringBuilder ret = new StringBuilder();

            private final Sink<?> JavaSink = t -> this.ret.append(postProcess(t.toString()) + "\n\n");

            private final Function<String, String> srcGen;

            private Cfr(Function<String, String> srcGen) {
                this.srcGen = srcGen;
            }

            private String postProcess(String decompiledSrc) {
                String str[] = ("extern \"C\" { \n" + decompiledSrc.replaceAll("(.*\\bpackage\\b.*\n)" // Remove --line '%package%'
                        + "|" + "(.*\\bimport\\b.*\n)" // Remove --line '%import%'
                        + "|" + "(\\bfinal\\b)" // Remove --word 'final'
                        + "|" + "(.*\\bextends\\b.*\n)" // Remove --line 'extends'
                        + "|" + "(.*\\bclass\\b.*\n)" // Remove --line 'class'
                        + "|" + "(.*\\bimplements\\b.*\n)" // Remove --line 'implements'
                        + "|" + "(\\bthis\\.\\b)" // Remove --word 'this'
                        // + "|" + "((?<=\\S|\\s)@\\S*\\s?)" // Remove --annotation
                        + "|" + "(\\bprotected\\b)" // Remove --word 'protected'
                        + "|" + "(\\bprivate\\b)" // Remove --word 'private'
                        + "|" + "(\\/\\*[\\s\\S]*?\\*\\/)" // Remove --comment
                        , "")).split("\n");

                final StringBuilder sb = new StringBuilder();

                for (String s : str) {
                    cudaGenerator(sb, this.srcGen.apply(s));
                }

                return sb.toString();
            }

            private void cudaGenerator(StringBuilder sb, String fragment) {
                sb.append(fragment);
                sb.append("\n");
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> Sink<T> getSink(SinkType sinkType, SinkClass sinkClass) {
                // Handle only JAVA output
                return sinkType == SinkType.JAVA ? (Sink<T>) this.JavaSink : null;
            }

            @Override
            public List<SinkClass> getSupportedSinks(SinkType sinkType, Collection<SinkClass> available) {
                if ((sinkType == SinkType.JAVA) && available.contains(SinkClass.STRING)) {
                    return Collections.singletonList(SinkClass.STRING);
                }

                return Collections.emptyList();
            }

            public String getResult() { return "//Decompiled with CFR 0.152.\n\n" + this.ret.toString(); }
        }

        // "(?<=__kernel__[^)]*\\(\\s*|,\\s*)"

        public String process(Cfr cfr, Class<? extends Program> cls) throws SwarmException {
            try {
                File f = File.createTempFile("swarm-", ".kernel");

                try (InputStream input = cls.getResourceAsStream("/" + cls.getName().replace('.', '/') + ".class");
                        OutputStream output = new FileOutputStream(f);) {

                    output.write(input.readAllBytes());
                }

                // Configure CFR decompiler
                CfrDriver driver = new CfrDriver.Builder().withOutputSink(cfr).build();

                // Run decompilation
                driver.analyse(Collections.singletonList(Path.of(f.getAbsolutePath()).toString()));

            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return "";
        }

        @Override
        public String process(List<Class<? extends Program>> cls, Function<String, String> srcGen) throws Exception {
            final Map<Thread, Cfr> wq = new HashMap<>();
            for (Class<? extends Program> c : cls) {
                Common.queue(() -> {
                    final Cfr cfr = wq.computeIfAbsent(Thread.currentThread(), k -> new Cfr(srcGen));
                    process(cfr, c);

                    return null;
                });
            }

            final StringBuilder sb = new StringBuilder();

            Common.await("Java Decompilation", 5);

            for (Cfr v : wq.values()) {
                sb.append(v.getResult());
            }

            return sb.toString();
        }

    }

    /**
     * Use CFR Java decompiler implementation <br/>
     * This is for proof of work and should be avoided as long as dedicated
     * decompiler for this specific purpose is available
     *
     * @return Default implementation (CFR Java Decompiler)
     */
    public static final Decompiler getDefault() { return new Default(); }

    /**
     * Process target-specific source.
     *
     * @param cls
     * @param srcGen
     * @return
     * @throws Exception
     */
    public abstract String process(List<Class<? extends Program>> cls, Function<String, String> srcGen) throws Exception;
}
