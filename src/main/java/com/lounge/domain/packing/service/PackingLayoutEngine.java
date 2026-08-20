package com.lounge.domain.packing.service;

import com.lounge.domain.packing.dto.PackingCheckResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Produces deterministic, relative placements for the client-side bag view.
 * This is a visual guide, not a physical collision simulation.
 */
@Component
public class PackingLayoutEngine {

    public List<PackingCheckResponse.Placement> layout(
            List<LayoutItem> items,
            double bagWidthMm,
            double bagHeightMm
    ) {
        List<PackingCheckResponse.Placement> placements = new ArrayList<>();
        double x = 5;
        double y = 5;
        double currentRowHeight = 0;
        double visualScale = visualScaleFor(items.size());

        for (LayoutItem item : items) {
            // Keep the real width-to-height relationship and only reduce all items
            // together as the manifest becomes fuller. This prevents a book from
            // looking larger than a laptop simply because it was added later.
            double widthPercent = clamp(item.widthMm() / bagWidthMm * visualScale, 8, 64);
            double heightPercent = clamp(item.heightMm() / bagHeightMm * visualScale, 7, 48);

            if (x + widthPercent > 95) {
                x = 5;
                y += currentRowHeight + 4;
                currentRowHeight = 0;
            }
            if (y + heightPercent > 95) {
                y = 5;
            }

            placements.add(new PackingCheckResponse.Placement(
                    item.itemCode(), item.itemName(), round(x), round(y),
                    round(widthPercent), round(heightPercent), 0
            ));
            x += widthPercent + 4;
            currentRowHeight = Math.max(currentRowHeight, heightPercent);
        }

        return List.copyOf(placements);
    }

    private double visualScaleFor(int itemCount) {
        return Math.max(40, 70 - Math.max(0, itemCount - 1) * 3.4);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public record LayoutItem(
            String itemCode,
            String itemName,
            double widthMm,
            double heightMm
    ) {
    }
}
