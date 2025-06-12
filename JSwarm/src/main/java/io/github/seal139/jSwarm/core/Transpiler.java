package io.github.seal139.jSwarm.core;

public interface Transpiler {
    String getAstResult();

    TranspileException getTranspileException();
}
