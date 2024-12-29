package io.github.seal139.jSwarm.core;

import io.github.seal139.jSwarm.runtime.datatype.Vector;

/**
 * Represent kernel or function
 */
public interface Kernel {

    /**
     * Get name of kernel.
     *
     * @return Name.
     */
    String getName();

    /**
     * Set function argument.
     *
     * @param index Parameter index to pass this argument.
     * @param data  Data to be set as an argument that passed to parameter based on
     *              index.
     */
    void setArgument(int index, Vector<? extends Number> data);

    /**
     * Set function arguments.
     *
     * @param datas Data to be set as an argument.
     */
    default void setArgument(Vector<? extends Number>... datas) {
        int i = -1;
        for (Vector<? extends Number> data : datas) {
            setArgument(++i, data);
        }
    }

    /**
     * Define working size based on ND-Range.
     *
     * @param dim Maximum size per dimension.
     */
    void setGlobalNDSize(long... dim);

    /**
     * Define local working group size based on ND-Range.
     *
     * @param dim Maximum size per dimension within group.
     */
    void setLocalNDSize(long... dim);
}
