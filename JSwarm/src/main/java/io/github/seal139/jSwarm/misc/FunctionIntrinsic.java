package io.github.seal139.jSwarm.misc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * It indicates that an annotated method may be intrinsified by the platform
 * target. Providing more faster and efficient process. The implementation
 * provided will be used if code run in JVM. Mostly for debugging purpose.
 *
 * <br/>
 * <br/>
 * The standard contract that must be satisfied by using this annotation is the
 * method called must give exactly the same result in any platform and the Java
 * implementation itself.
 */

@Retention(RetentionPolicy.SOURCE)
public @interface FunctionIntrinsic {

}
