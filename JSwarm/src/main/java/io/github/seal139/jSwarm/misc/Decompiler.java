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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

            public static String replaceAll(String text, Map<Pattern, String> replacements) {
                StringBuilder result = new StringBuilder(text);

                for (Map.Entry<Pattern, String> entry : replacements.entrySet()) {
                    Pattern pattern = entry.getKey();
                    Matcher matcher = pattern.matcher(result);

                    int          lastIndex = 0;
                    StringBuffer temp      = new StringBuffer();

                    while (matcher.find()) {
                        matcher.appendReplacement(temp, Matcher.quoteReplacement(entry.getValue()));
                    }
                    matcher.appendTail(temp);

                    // Update result efficiently
                    result.setLength(0);
                    result.append(temp);
                }
                return result.toString();
            }

            private String functionHeader(String src) {
                return null;
            }

            private String functionBody(String src) {
                return null;
            }

            /**
             * Handle java array to make it compatible with C or C++ style
             *
             * @param decompiledSrc
             * @return
             */
            private String sanitizeArray(String decompiledSrc) {
                /* Used to re-arrange array with predefined size */ {
                    Pattern pattern = Pattern.compile( //
                            "(\\b(int|Integer|long|Long|short|Short|float|Float|double|Double))\\[\\]\\s+(\\w+)\\s*=\\s*new\\s+\\2\\[(\\d+)\\]" //
                    );

                    Matcher matcher = pattern.matcher(decompiledSrc);
                    decompiledSrc = matcher.replaceAll("$1 $3[$4]");
                }

                /* Used to re-arrange array with predefined value */ {
                    Pattern pattern = Pattern.compile( //
                            "(\\b(int|Integer|long|Long|short|Short|float|Float|double|Double))\\[\\]\\s+(\\w+)\\s*=\\s*new\\s+\\2\\[\\]" //
                    );

                    Matcher matcher = pattern.matcher(decompiledSrc);
                    decompiledSrc = matcher.replaceAll("$2 $3[] = ");
                }

                return decompiledSrc;
            }

            /**
             * Used to rearrange single assignment a = b = c = d;
             *
             * @param decompiledSrc
             * @return
             */
            private String sanitizeSingleAssignment(String decompiledSrc) {

                String  pattern = "((.*)\\b(int|Integer|long|Long|short|Short|float|Float|double|Double).+=.+=.+)"; // Detect multiple space
                                                                                                                    // within
                                                                                                                    // the same line
                Pattern regex   = Pattern.compile(pattern);
                Matcher matcher = regex.matcher(decompiledSrc);

                while (matcher.find()) {

                    StringBuilder replacement = new StringBuilder();

                    // it hard to use regex without manual parsing
                    // So, in this case, we will loop through the string in reverse order to get the
                    // first assignment order;

                    char[] refChar = matcher.group().toCharArray();
                    int    length  = refChar.length - 1;

                    // We are looking for 2nd = after 1st = to determine the bound
                    String prevVariable = null;

                    StringBuilder variable  = new StringBuilder();
                    StringBuilder sanitizer = new StringBuilder();

                    for (int i = length; i >= 0; i--) {
                        if (refChar[i] == '=') {
                            char[] token = variable.reverse().toString().trim().toCharArray();

                            // Sanitize parenthesis
                            int tokenLength = token.length;
                            int equilibrium = 0;
                            for (int l = 0; l < tokenLength; l++) {
                                if (token[l] == '(') {
                                    equilibrium--;
                                }
                                else if (token[l] == ')') {
                                    if (equilibrium == 0) {
                                        continue;
                                    }

                                    equilibrium++;
                                }

                                sanitizer.append(token[l]);
                            }

                            String assignment = sanitizer.toString();
                            sanitizer.setLength(0);

                            if (prevVariable != null) {
                                assignment = assignment.replaceAll("\\)|\\(", "");
                                replacement.append(matcher.group(2) + assignment + prevVariable + ";\n");
                            }

                            prevVariable = " = " + assignment;

                            variable.setLength(0);
                        }
                        else {
                            variable.append(refChar[i]);
                        }
                    }

                    replacement.append(variable.reverse().toString() + prevVariable + ";");

                    decompiledSrc = matcher.replaceFirst(replacement.toString());
                }

                return decompiledSrc;
            }

            private String aliases(String decompiledSrc) {
                return """
                        typedef int Integer;
                        typedef long Long;
                        typedef short Short;
                        typedef double Double;
                        typedef float Float;
                        """ + decompiledSrc;
            }

            private String commentRemover(String decompiledSrc) {
                // Use Pattern.DOTALL to match across multiple lines
                Pattern pattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(decompiledSrc);

                // Remove all block comments
                return matcher.replaceAll("");
            }

            private String postProcess(String decompiledSrc) {
                Pattern headerPattern = Pattern.compile("^(public|protected).*\\{$");

                decompiledSrc = sanitizeArray(decompiledSrc);
                decompiledSrc = sanitizeSingleAssignment(decompiledSrc);
                decompiledSrc = aliases(decompiledSrc);
                decompiledSrc = commentRemover(decompiledSrc);

                Map<Pattern, String> genericReplacement = new HashMap<>();
                genericReplacement.put(Pattern.compile("(\\bprotected\\b)"), "__device__");
                genericReplacement.put(Pattern.compile("(\\bpublic\\b)"), "__kernel__");

                genericReplacement.put(Pattern.compile("(\\bfloat\\[\\])"), "float*");
                genericReplacement.put(Pattern.compile("(\\bdouble\\[\\])"), "double*");
                genericReplacement.put(Pattern.compile("(\\bshort\\[\\])"), "short*");
                genericReplacement.put(Pattern.compile("(\\bint\\[\\])"), "int*");
                genericReplacement.put(Pattern.compile("(\\blong\\[\\])"), "long*");

                try {

                    System.out.println(replaceAll(decompiledSrc, genericReplacement));
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }

                String str[] = ("extern \"C\" { \n" + decompiledSrc.replaceAll("(.*\\bpackage\\b.*\n)" // Remove --line '%package%'
                        + "|" + "(.*\\bimport\\b.*\n)" // Remove --line '%import%'
                        + "|" + "(\\bfinal\\b)" // Remove --word 'final'
                        + "|" + "(.*\\bextends\\b.*\n)" // Remove --line 'extends'
                        + "|" + "(.*\\bclass\\b.*\n)" // Remove --line 'class'
                        + "|" + "(.*\\bimplements\\b.*\n)" // Remove --line 'implements'
                        + "|" + "(\\bthis\\.\\b)" // Remove --word 'this'
                        // + "|" + "((?<=\\S|\\s)@\\S*\\s?)" // Remove --annotation
                        + "|" + "" // Remove --word 'protected'
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
