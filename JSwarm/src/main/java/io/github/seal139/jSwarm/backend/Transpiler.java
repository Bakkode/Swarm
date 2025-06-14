package io.github.seal139.jSwarm.backend;

import io.github.seal139.jSwarm.runtime.TranspileException;

public interface Transpiler {
    String getTranspiledSource();

    TranspileException getTranspileException();
}
