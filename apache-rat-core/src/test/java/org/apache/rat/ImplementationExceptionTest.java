package org.apache.rat;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImplementationExceptionTest {

    @Test
    void fromTest() {
        RuntimeException runTime = new RuntimeException();
        assertThat(ImplementationException.makeInstance(runTime))
                .isInstanceOf(ImplementationException.class)
                .hasCause(runTime);

        Exception ex = new Exception();
        assertThat(ImplementationException.makeInstance(ex))
                .isInstanceOf(ImplementationException.class)
                .hasCause(ex);

        ex = new ImplementationException("yee haw");
        assertThat(ImplementationException.makeInstance(ex))
                .isInstanceOf(ImplementationException.class)
                .isEqualTo(ex);
    }
}
