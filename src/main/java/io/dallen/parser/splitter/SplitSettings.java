package io.dallen.parser.splitter;

import java.util.ArrayList;
import java.util.List;

/**
 * Define the cases which the multileveled split will operate on.
 */
public class SplitSettings {
    private final List<SplitLayer> splitLayers = new ArrayList<>();

    public SplitSettings addLayer(SplitLayer layer) {
        splitLayers.add(layer);
        return this;
    }

    public SplitSettings leftToRight(boolean leftToRight) {
        splitLayers.get(splitLayers.size() - 1).leftToRight(leftToRight);
        return this;
    }

    List<SplitLayer> getSplitLayers() {
        return splitLayers;
    }
}
