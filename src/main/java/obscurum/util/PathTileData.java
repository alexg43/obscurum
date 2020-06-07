package obscurum.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Point;

@RequiredArgsConstructor
@Getter
public class PathTileData {
    private final Point location;
    private final int priority;
}
