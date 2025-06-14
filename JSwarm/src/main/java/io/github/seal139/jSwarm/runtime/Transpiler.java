package io.github.seal139.jSwarm.runtime;

public interface Transpiler {
    String getAstResult();

    TranspileException getTranspileException();
}
