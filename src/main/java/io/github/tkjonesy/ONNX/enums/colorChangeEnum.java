package io.github.tkjonesy.ONNX.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum colorChangeEnum {
    BOUNDINGBOX(1),
    LOGADDED(2),
    LOGREMOVED(3);

    private final int code;
}
