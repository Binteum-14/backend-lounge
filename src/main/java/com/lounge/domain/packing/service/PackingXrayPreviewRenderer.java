package com.lounge.domain.packing.service;

import com.lounge.domain.packing.PackingStatus;
import com.lounge.domain.packing.dto.PackingCheckResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renders the analysis result as a browser-ready SVG packing preview.
 */
@Component
public class PackingXrayPreviewRenderer {

    private static final int CANVAS_WIDTH = 1440;
    private static final int CANVAS_HEIGHT = 900;

    private static final String[] ITEM_COLORS = {
            "#B96F3D", "#D6AD68", "#E8D7BA", "#8F5C3E", "#C9864C", "#A77B55"
    };
    private final Map<String, ArtworkBounds> artworkBoundsCache = new ConcurrentHashMap<>();

    public String render(PackingCheckResponse response) {
        BagFrame frame = BagFrame.from(response.scene());
        ArtworkLayout artwork = artworkLayout(response.imageUrl(), frame);
        String bagImage = embeddedImageDataUri(response.imageUrl());
        StringBuilder svg = new StringBuilder();

        svg.append("""
                <svg xmlns="http://www.w3.org/2000/svg" width="1440" height="900" viewBox="0 0 1440 900" role="img">
                  <defs>
                    <linearGradient id="background" x1="0" x2="0" y1="0" y2="1">
                      <stop stop-color="#F4E9DA"/><stop offset="0.52" stop-color="#EBD9C4"/><stop offset="1" stop-color="#DCC1A5"/>
                    </linearGradient>
                    <linearGradient id="scan" x1="0" x2="0" y1="0" y2="1">
                      <stop stop-color="#FFFFFF" stop-opacity="0"/>
                      <stop offset="0.47" stop-color="#E2B978" stop-opacity="0.08"/>
                      <stop offset="0.50" stop-color="#FFF0D5" stop-opacity="0.52"/>
                      <stop offset="0.53" stop-color="#C9864C" stop-opacity="0.10"/>
                      <stop offset="1" stop-color="#FFFFFF" stop-opacity="0"/>
                    </linearGradient>
                    <linearGradient id="bagGlow" x1="0" x2="1" y1="0" y2="1">
                      <stop stop-color="#E5C18C" stop-opacity="0.46"/><stop offset="0.5" stop-color="#845133" stop-opacity="0.20"/><stop offset="1" stop-color="#C68A51" stop-opacity="0.38"/>
                    </linearGradient>
                    <pattern id="grid" width="28" height="28" patternUnits="userSpaceOnUse">
                      <path d="M 28 0 L 0 0 0 28" fill="none" stroke="#A97652" stroke-opacity="0.18" stroke-width="1"/>
                    </pattern>
                    <filter id="glow"><feGaussianBlur stdDeviation="5" result="blur"/><feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge></filter>
                    <filter id="softGlow"><feGaussianBlur stdDeviation="12"/></filter>
                    <filter id="xrayImage"><feColorMatrix type="matrix" values="0.42 0.18 0.08 0 0.20 0.24 0.12 0.05 0 0.12 0.10 0.05 0.02 0 0.06 0 0 0 0.78 0"/></filter>
                    <clipPath id="bagClip"><rect x="%s" y="%s" width="%s" height="%s" rx="%s"/></clipPath>
                    <mask id="bagShapeMask" maskUnits="userSpaceOnUse" x="%s" y="%s" width="%s" height="%s" style="mask-type:alpha">
                      <image href="%s" x="%s" y="%s" width="%s" height="%s" preserveAspectRatio="xMidYMid meet"/>
                    </mask>
                  </defs>
                  <rect width="1440" height="900" fill="url(#background)"/>
                  <rect width="1440" height="900" fill="url(#grid)"/>
                  <circle cx="720" cy="410" r="390" fill="#C9864C" fill-opacity="0.14" filter="url(#softGlow)"/>
                  <rect x="38" y="28" width="1364" height="72" rx="12" fill="#160E0A" fill-opacity="0.94" stroke="#795137" stroke-width="1.5"/>
                  <circle cx="76" cy="64" r="11" fill="#D6AD68" filter="url(#glow)"/><circle cx="76" cy="64" r="4" fill="#FFF1D7"/>
                  <text x="102" y="58" fill="#F5E8D4" font-family="Arial, sans-serif" font-size="24" font-weight="800" letter-spacing="1">AI PACKING · SECURITY SCREENING</text>
                  <text x="103" y="81" fill="#C7AD8C" font-family="Arial, sans-serif" font-size="12" letter-spacing="1.4">MCM LOUNGE / PACKING CLEARANCE</text>
                  <text x="1364" y="59" fill="#E4D1B2" text-anchor="end" font-family="Arial, sans-serif" font-size="14" font-weight="700">VISÉTOS · ONLINE</text>
                  <text x="1364" y="81" fill="#B79875" text-anchor="end" font-family="Arial, sans-serif" font-size="12">%s</text>
                """.formatted(
                number(frame.x()), number(frame.y()), number(frame.width()), number(frame.height()), number(frame.radius()),
                number(frame.x()), number(frame.y()), number(frame.width()), number(frame.height()),
                bagImage, number(artwork.x()), number(artwork.y()), number(artwork.width()), number(artwork.height()),
                escape(response.productName())
        ));

        appendSidebar(svg, response);
        appendBag(svg, response, frame, artwork, bagImage);
        appendItems(svg, response, frame);
        appendNoPackableItemsMessage(svg, response, frame);
        appendScanOverlay(svg, frame);
        appendResultPanel(svg, response);
        appendSpaceUsed(svg, response);
        svg.append("</svg>");

        return svg.toString();
    }

    private void appendSidebar(StringBuilder svg, PackingCheckResponse response) {
        int notPackedCount = (int) response.items().stream()
                .filter(item -> !item.fit())
                .count();
        int visibleItemCount = Math.min(10, response.items().size());
        boolean denseManifest = visibleItemCount > 7;
        double firstItemY = denseManifest ? 218 : 232;
        double rowHeight = denseManifest ? 53 : 68;
        double sidebarHeight = Math.max(
                548,
                Math.min(620, firstItemY + (visibleItemCount - 1) * rowHeight + 38 - 132)
        );
        svg.append("""
                  <rect x="58" y="132" width="270" height="%s" rx="16" fill="#150E0A" fill-opacity="0.95" stroke="#725039" stroke-width="1.5"/>
                  <rect x="58" y="132" width="270" height="38" rx="16" fill="#352116"/><path d="M58 154 H328" stroke="#725039"/>
                  <text x="86" y="157" fill="#E4C89E" font-family="Arial, sans-serif" font-size="12" font-weight="800" letter-spacing="1.2">SCREENING MANIFEST</text>
                  <text x="298" y="157" fill="#F4E6D0" text-anchor="end" font-family="Arial, sans-serif" font-size="12" font-weight="700">%d FIT / %d</text>
                  <text x="86" y="197" fill="#B59A7B" font-family="Arial, sans-serif" font-size="11" letter-spacing="1">DETECTED CONTENTS</text>
                """.formatted(number(sidebarHeight), response.placements().size(), response.items().size()));
        if (notPackedCount > 0) {
            svg.append("<text x=\"298\" y=\"197\" text-anchor=\"end\" fill=\"#FF9AA4\" font-family=\"Arial, sans-serif\" font-size=\"10\" font-weight=\"800\">OUT · ")
                    .append(notPackedCount).append("</text>");
        }

        for (int index = 0; index < visibleItemCount; index++) {
            PackingCheckResponse.ItemResult item = response.items().get(index);
            double y = firstItemY + index * rowHeight;
            String color = item.fit() ? "#E1C28E" : "#8C7462";
            svg.append("<g opacity=\"").append(item.fit() ? "1" : "0.48").append("\">");
            appendItemIcon(svg, item.itemCode(), 88, y - 18, color);
            svg.append("""
                      <text x="132" y="%s" fill="#E4ECF7" font-family="Arial, sans-serif" font-size="%s" font-weight="600">%s</text>
                      <text x="132" y="%s" fill="#7F91A8" font-family="Arial, sans-serif" font-size="%s">%s</text>
                      <rect x="%s" y="%s" width="%s" height="20" rx="4" fill="%s"/>
                      <text x="%s" y="%s" text-anchor="middle" fill="%s" font-family="Arial, sans-serif" font-size="%s" font-weight="800">%s</text>
                    """.formatted(
                    number(y - 2), denseManifest ? "13" : "15", escape(item.itemName()), number(y + 15), denseManifest ? "10" : "12", escape(item.itemCode()),
                    item.fit() ? "277" : "257", number(y - 18), item.fit() ? "20" : "40", item.fit() ? "#D6AD68" : "#4B2D38",
                    item.fit() ? "287" : "277", number(y - 3), item.fit() ? "#211208" : "#FFD9DE", item.fit() ? "15" : "9", item.fit() ? "✓" : "OUT"
            ));
            svg.append("</g>");
        }
        if (response.items().size() > visibleItemCount) {
            svg.append("<text x=\"298\" y=\"")
                    .append(number(132 + sidebarHeight - 16))
                    .append("\" text-anchor=\"end\" fill=\"#C7AD8C\" font-family=\"Arial, sans-serif\" font-size=\"10\" font-weight=\"700\">+")
                    .append(response.items().size() - visibleItemCount)
                    .append(" MORE ITEMS</text>");
        }
    }

    private void appendBag(
            StringBuilder svg,
            PackingCheckResponse response,
            BagFrame frame,
            ArtworkLayout artwork,
            String bagImage
    ) {
        svg.append("""
                  <g>
                    <rect x="390" y="110" width="660" height="658" rx="42" fill="#120B08" fill-opacity="0.94" stroke="#80563A" stroke-width="2"/>
                    <rect x="409" y="130" width="622" height="620" rx="28" fill="#28170F" stroke="#A47449" stroke-width="1.5"/>
                    <path d="M420 161 H1020 M420 720 H1020" stroke="#D1AC78" stroke-opacity="0.38"/>
                    <text x="438" y="157" fill="#D4B892" font-family="Arial, sans-serif" font-size="11" font-weight="700" letter-spacing="1.5">MCM PACKING · X-RAY VIEW</text>
                    <text x="1005" y="157" fill="#E1B56F" text-anchor="end" font-family="Arial, sans-serif" font-size="11" font-weight="800">CLEAR</text>
                    <rect x="%s" y="%s" width="%s" height="%s" rx="%s" fill="#C9864C" fill-opacity="0.07" filter="url(#softGlow)"/>
                    <rect x="%s" y="%s" width="%s" height="%s" rx="%s" fill="#B17545" fill-opacity="0.03" stroke="#E7C993" stroke-opacity="0.20" stroke-width="1"/>
                    <image href="%s" x="%s" y="%s" width="%s" height="%s" opacity="0.38" preserveAspectRatio="xMidYMid meet" filter="url(#xrayImage)"/>
                    <rect x="%s" y="%s" width="%s" height="%s" rx="%s" fill="url(#bagGlow)" fill-opacity="0.48" mask="url(#bagShapeMask)"/>
                    <rect x="%s" y="%s" width="%s" height="%s" rx="%s" fill="none" stroke="#C9E0FF" stroke-opacity="0" stroke-width="2"/>
                    <rect x="%s" y="%s" width="%s" height="28" fill="#F0D7A9" fill-opacity="0.08" mask="url(#bagShapeMask)"/>
                    <path d="M %s %s H %s M %s %s H %s" stroke="#E4C28C" stroke-opacity="0.50" stroke-width="1"/>
                    <path d="M 360 774 H1080" stroke="#21140D" stroke-width="26"/><path d="M 360 774 H1080" stroke="#976441" stroke-opacity="0.70" stroke-width="2" stroke-dasharray="12 10"/>
                    <path d="M 422 774 H1018" stroke="#E3C796" stroke-opacity="0.32" stroke-width="2"/>
                  </g>
                """.formatted(
                number(frame.x()), number(frame.y()), number(frame.width()), number(frame.height()), number(frame.radius()),
                number(frame.x()), number(frame.y()), number(frame.width()), number(frame.height()), number(frame.radius()),
                bagImage, number(artwork.x()), number(artwork.y()), number(artwork.width()), number(artwork.height()),
                number(frame.x()), number(frame.y()), number(frame.width()), number(frame.height()), number(frame.radius()),
                number(frame.x()), number(frame.y()), number(frame.width()), number(frame.height()), number(frame.radius()),
                number(frame.x()), number(frame.y() + frame.height() * 0.48), number(frame.width()),
                number(frame.x() + 28), number(frame.y() + 36), number(frame.x() + frame.width() - 28),
                number(frame.x() + 28), number(frame.y() + frame.height() - 36), number(frame.x() + frame.width() - 28)
        ));
    }

    private void appendItems(
            StringBuilder svg,
            PackingCheckResponse response,
            BagFrame frame
    ) {
        List<PackingCheckResponse.Placement> placements = response.placements();
        if (placements.isEmpty()) {
            return;
        }

        // The product-photo silhouette is intentionally irregular (handles,
        // tapered shoulders and straps). It is useful for tinting the bag but
        // must never crop a selected item in half. Contents stay within the
        // scanner frame while retaining the safe inner placement margins.
        svg.append("<g data-packing-contents=\"true\" clip-path=\"url(#bagClip)\">");
        // This is the actual packing cavity, not the full product-photo
        // bounding box. Keeping every item's whole bounding box in this zone
        // prevents both the old cropped result and the later overflow result.
        PackingZone packingZone = PackingZone.forProfile(response, frame);
        double packingX = packingZone.x();
        double packingY = packingZone.y();
        double packingWidth = packingZone.width();
        double packingHeight = packingZone.height();
        PackingCheckResponse.Placement primaryItem = findPrimaryItem(placements);
        double layoutScale = PackingLayoutEngine.visualScaleFor(placements.size());
        ItemVisualSize primaryVisualSize = physicalVisualSize(primaryItem, response, packingZone);
        // PackingLayoutEngine stores values after applying its layout-only
        // scale. Undo that scale here: the rendered X-ray must use the actual
        // item-to-bag proportion, not a second miniature version of it.
        double primaryWidth = packingWidth * primaryItem.width() / layoutScale;
        double primaryHeight = packingHeight * primaryItem.height() / layoutScale;
        if (primaryItem.itemCode().startsWith("LAPTOP")) {
            // A laptop is normally placed against the wide rear sleeve of a
            // backpack. The raw layout percentage is deliberately conservative
            // for collision checks, but looked far too small in the X-ray.
            // Give a fitting laptop the dominant rear-sleeve footprint it
            // actually has in the bag, clearly larger than a tablet or book.
            primaryWidth = Math.min(
                    frame.width() * 0.66,
                    Math.max(primaryWidth, packingWidth * 1.14)
            );
            primaryHeight = Math.max(primaryHeight, packingHeight * 0.58);
        } else if (!"TABLET_11".equals(primaryItem.itemCode())) {
            primaryWidth = primaryVisualSize.width();
            primaryHeight = primaryVisualSize.height();
        }
        // The largest item is the stable visual anchor. In particular, this
        // keeps a laptop in the real centre of the bag instead of letting the
        // normal flow-layout order push it to an edge.
        double primaryX = packingX + (packingWidth - primaryWidth) / 2;
        double primaryY = packingY + (packingHeight - primaryHeight) / 2;
        double[][] peripheralTargets = "P03".equals(response.loungeId())
                ? new double[][] {
                // The slim crossbody pouch is a horizontal cavity: keep the
                // phone on top of the pouch, but wholly within its body.
                {0.25, 0.52}, {0.75, 0.52}, {0.50, 0.25}
        }
                : primaryItem.itemCode().startsWith("LAPTOP")
                ? new double[][] {
                // Supporting items sit at the edge of a laptop sleeve instead
                // of covering the large device in the middle of the bag.
                {0.16, 0.18}, {0.84, 0.18}, {0.16, 0.78},
                {0.84, 0.78}, {0.15, 0.50}, {0.85, 0.50}
        }
                : new double[][] {
                // Keep every supporting item in the bag's central packing
                // zone. The old 0.22/0.78 orbit looked scattered and caused
                // tall items to appear clipped by a bag's handle or edge.
                {0.33, 0.36}, {0.67, 0.36}, {0.29, 0.68},
                {0.71, 0.68}, {0.50, 0.25}, {0.50, 0.80},
                {0.22, 0.50}, {0.78, 0.50}, {0.34, 0.82},
                {0.66, 0.82}, {0.50, 0.68}
        };
        List<PackingCheckResponse.Placement> renderOrder = new ArrayList<>();
        renderOrder.add(primaryItem);
        placements.stream()
                .filter(placement -> placement != primaryItem)
                .forEach(renderOrder::add);
        int peripheralIndex = 0;
        for (int index = 0; index < renderOrder.size(); index++) {
            PackingCheckResponse.Placement placement = renderOrder.get(index);
            String color = ITEM_COLORS[index % ITEM_COLORS.length];
            boolean primary = placement == primaryItem;
            double width = packingWidth * placement.width() / layoutScale;
            double height = packingHeight * placement.height() / layoutScale;
            if (!placement.itemCode().startsWith("LAPTOP")
                    && !"TABLET_11".equals(placement.itemCode())) {
                ItemVisualSize visualSize = physicalVisualSize(placement, response, packingZone);
                width = visualSize.width();
                height = visualSize.height();
            }
            double x;
            double y;
            if (primary) {
                // Apply the calculated main-item footprint. Previously this
                // branch used the centred position but accidentally kept the
                // old, tiny dimensions when it drew the image.
                width = primaryWidth;
                height = primaryHeight;
                x = primaryX;
                y = primaryY;
            } else {
                // Secondary items remain proportionate to one another but
                // receive a modest visual reduction so they remain legible
                // around the main item instead of being hidden below it.
                width *= 0.80;
                height *= 0.80;
                double[] target = peripheralTargets[peripheralIndex % peripheralTargets.length];
                peripheralIndex++;
                x = clamp(packingX + packingWidth * target[0] - width / 2, packingX, packingX + packingWidth - width);
                y = clamp(packingY + packingHeight * target[1] - height / 2, packingY, packingY + packingHeight - height);
            }
            svg.append("<g data-item=\"").append(escape(placement.itemCode()))
                    .append("\" data-primary=\"").append(primary).append("\">");
            appendItemShape(svg, placement, x, y, width, height, color);
            svg.append("</g>");
        }
        svg.append("</g>");
    }

    private void appendNoPackableItemsMessage(
            StringBuilder svg,
            PackingCheckResponse response,
            BagFrame frame
    ) {
        if (!response.placements().isEmpty() || response.items().isEmpty()) {
            return;
        }

        String itemName = response.items().getFirst().itemName();
        svg.append("""
                  <g mask="url(#bagShapeMask)">
                    <rect x="%s" y="%s" width="%s" height="92" rx="16" fill="#07131F" fill-opacity="0.82" stroke="#E2707A" stroke-opacity="0.68"/>
                    <text x="%s" y="%s" text-anchor="middle" fill="#FFD6DB" font-family="Arial, sans-serif" font-size="16" font-weight="800" letter-spacing="1.4">NO PACKABLE ITEMS</text>
                    <text x="%s" y="%s" text-anchor="middle" fill="#D9AAB0" font-family="Arial, sans-serif" font-size="13">%s은(는) 이 가방에 수납할 수 없습니다</text>
                  </g>
                """.formatted(
                number(frame.x() + frame.width() * 0.12), number(frame.y() + frame.height() * 0.48 - 46), number(frame.width() * 0.76),
                number(frame.x() + frame.width() / 2), number(frame.y() + frame.height() * 0.48 - 12),
                number(frame.x() + frame.width() / 2), number(frame.y() + frame.height() * 0.48 + 18), escape(itemName)
        ));
    }

    private PackingCheckResponse.Placement findPrimaryItem(
            List<PackingCheckResponse.Placement> placements
    ) {
        return placements.stream()
                .filter(placement -> placement.itemCode().startsWith("LAPTOP"))
                .findFirst()
                .orElseGet(() -> placements.stream()
                        .max(java.util.Comparator.comparingDouble(
                                placement -> placement.width() * placement.height()
                        ))
                        .orElseThrow());
    }

    private void appendScanOverlay(StringBuilder svg, BagFrame frame) {
        double centerX = frame.x() + frame.width() / 2;
        svg.append("""
                  <g mask="url(#bagShapeMask)" pointer-events="none">
                    <rect x="%s" y="%s" width="%s" height="4" fill="#F1D19C" fill-opacity="0.74" filter="url(#glow)">
                      <animate attributeName="y" values="%s;%s;%s" dur="2.8s" repeatCount="indefinite"/>
                    </rect>
                    <rect x="%s" y="%s" width="%s" height="46" fill="#D29A5D" fill-opacity="0.08">
                      <animate attributeName="y" values="%s;%s;%s" dur="2.8s" repeatCount="indefinite"/>
                    </rect>
                    <path d="M %s %s V %s M %s %s H %s" stroke="#F0D4A4" stroke-opacity="0.48" stroke-width="1.5"/>
                    <circle cx="%s" cy="%s" r="8" fill="none" stroke="#F0D4A4" stroke-opacity="0.48"/>
                    <text x="%s" y="%s" fill="#E5C79A" fill-opacity="0.68" text-anchor="middle" font-family="Arial, sans-serif" font-size="10" letter-spacing="1.6">MATERIAL DENSITY SCAN</text>
                  </g>
                """.formatted(
                number(frame.x()), number(frame.y()), number(frame.width()),
                number(frame.y() + 36), number(frame.y() + frame.height() - 40), number(frame.y() + 36),
                number(frame.x()), number(frame.y()), number(frame.width()),
                number(frame.y() + 16), number(frame.y() + frame.height() - 62), number(frame.y() + 16),
                number(centerX), number(frame.y() + 22), number(frame.y() + 46),
                number(centerX - 12), number(frame.y() + 34), number(centerX + 12),
                number(centerX), number(frame.y() + 34),
                number(centerX), number(frame.y() + frame.height() - 17)
        ));
    }

    private void appendResultPanel(StringBuilder svg, PackingCheckResponse response) {
        String statusColor = statusColor(response.status());
        String statusDetail = statusDetail(response);
        double usedWidth = 196 * Math.min(1, response.usedSpaceRatio());
        double fitWidth = 196 * response.fitScore() / 100.0;
        svg.append("""
                  <rect x="1102" y="132" width="280" height="548" rx="16" fill="#150E0A" fill-opacity="0.95" stroke="#725039" stroke-width="1.5"/>
                  <rect x="1102" y="132" width="280" height="38" rx="16" fill="#352116"/><path d="M1102 154 H1382" stroke="#725039"/>
                  <text x="1130" y="157" fill="#E4C89E" font-family="Arial, sans-serif" font-size="12" font-weight="800" letter-spacing="1.2">SCREENING RESULT</text>
                  <text x="1130" y="218" fill="#F4E6D0" font-family="Arial, sans-serif" font-size="18" font-weight="700">%s</text>
                  <line x1="1130" y1="240" x2="1354" y2="240" stroke="#513524"/>
                  <text x="1130" y="284" fill="#BDA183" font-family="Arial, sans-serif" font-size="13" font-weight="700">수납률</text>
                  <text x="1130" y="326" fill="#F0D6AC" font-family="Arial, sans-serif" font-size="40" font-weight="500">%.0f%%</text>
                  <rect x="1130" y="347" width="196" height="7" rx="3.5" fill="#352419"/><rect x="1130" y="347" width="%s" height="7" rx="3.5" fill="#C9864C"/>
                  <text x="1130" y="405" fill="#BDA183" font-family="Arial, sans-serif" font-size="13" font-weight="700">예상 적합도</text>
                  <text x="1130" y="447" fill="#F0D6AC" font-family="Arial, sans-serif" font-size="40" font-weight="500">%d%%</text>
                  <rect x="1130" y="468" width="196" height="7" rx="3.5" fill="#352419"/><rect x="1130" y="468" width="%s" height="7" rx="3.5" fill="#D6AD68"/>
                  <line x1="1130" y1="518" x2="1354" y2="518" stroke="#513524"/>
                  <text x="1130" y="555" fill="#BDA183" font-family="Arial, sans-serif" font-size="13" font-weight="700">CLEARANCE</text>
                  <circle cx="1141" cy="588" r="6" fill="%s"/><text x="1158" y="594" fill="#F2E2CC" font-family="Arial, sans-serif" font-size="15" font-weight="600">%s</text>
                  <text x="1130" y="630" fill="#B39A7F" font-family="Arial, sans-serif" font-size="13">%s</text>
                """.formatted(
                escape(response.productName()), response.usedSpaceRatio() * 100, number(usedWidth),
                response.fitScore(), number(fitWidth), statusColor, escape(statusLabel(response.status())),
                escape(statusDetail)
        ));
    }

    private String statusDetail(PackingCheckResponse response) {
        List<String> notPackedItemNames = response.items().stream()
                .filter(item -> !item.fit())
                .map(PackingCheckResponse.ItemResult::itemName)
                .toList();
        if (notPackedItemNames.size() == 1) {
            return notPackedItemNames.getFirst() + " 제외 필요";
        }
        if (notPackedItemNames.size() > 1) {
            return notPackedItemNames.size() + "개 물품 수납 불가";
        }
        return switch (response.scene()) {
                    case "perfume" -> "Perfume set bag profile";
                    case "snack" -> "Snack and drink set bag profile";
                    case "flight" -> "Flight baggage profile";
                    default -> "Lounge baggage profile";
                };
    }

    private void appendSpaceUsed(StringBuilder svg, PackingCheckResponse response) {
        double progress = Math.min(1, response.usedSpaceRatio());
        StringBuilder bars = new StringBuilder();
        for (int index = 0; index < 20; index++) {
            bars.append("<rect x=\"").append(464 + index * 22).append("\" y=\"817\" width=\"14\" height=\"18\" rx=\"2\" fill=\"")
                    .append(index < Math.ceil(progress * 20) ? "#D6AD68" : "#352419").append("\"/>");
        }
        svg.append("""
                  <rect x="430" y="777" width="580" height="92" rx="20" fill="#160E0A" fill-opacity="0.92" stroke="#674631" stroke-width="1.5"/>
                  <text x="464" y="808" fill="#BFA283" font-family="Arial, sans-serif" font-size="13" font-weight="700">SPACE USED</text>
                  <text x="976" y="808" text-anchor="end" fill="#F0D6AC" font-family="Arial, sans-serif" font-size="31" font-weight="500">%.0f%%</text>
                """.formatted(response.usedSpaceRatio() * 100));
        svg.append(bars);
    }

    private void appendItemIcon(
            StringBuilder svg,
            String itemCode,
            double x,
            double y,
            String color
    ) {
        String stroke = " stroke=\"" + color + "\" stroke-width=\"2\" fill=\"none\"";
        switch (itemCode) {
            case "LAPTOP_13", "LAPTOP_14" -> svg.append("<rect x=\"").append(number(x)).append("\" y=\"").append(number(y)).append("\" width=\"28\" height=\"19\" rx=\"2\"").append(stroke).append("/><path d=\"M ").append(number(x - 4)).append(" ").append(number(y + 23)).append(" H ").append(number(x + 32)).append("\"").append(stroke).append("/>");
            case "TABLET_11" -> svg.append("<rect x=\"").append(number(x + 5)).append("\" y=\"").append(number(y - 2)).append("\" width=\"19\" height=\"29\" rx=\"3\"").append(stroke).append("/><circle cx=\"").append(number(x + 14.5)).append("\" cy=\"").append(number(y + 22)).append("\" r=\"1.4\" fill=\"").append(color).append("\"/>");
            case "SMARTPHONE", "POWER_BANK" -> svg.append("<rect x=\"").append(number(x + 7)).append("\" y=\"").append(number(y - 2)).append("\" width=\"15\" height=\"29\" rx=\"3\"").append(stroke).append("/><circle cx=\"").append(number(x + 14.5)).append("\" cy=\"").append(number(y + 22)).append("\" r=\"1.2\" fill=\"").append(color).append("\"/>");
            case "TUMBLER" -> svg.append("<rect x=\"").append(number(x + 8)).append("\" y=\"").append(number(y + 2)).append("\" width=\"13\" height=\"25\" rx=\"5\"").append(stroke).append("/><path d=\"M ").append(number(x + 10)).append(" ").append(number(y + 2)).append(" V ").append(number(y - 2)).append(" H ").append(number(x + 19)).append(" V ").append(number(y + 2)).append("\"").append(stroke).append("/>");
            case "BOOK_PAPERBACK", "NOTEBOOK_A5", "PASSPORT" -> svg.append("<path d=\"M ").append(number(x + 5)).append(" ").append(number(y + 2)).append(" H ").append(number(x + 23)).append(" V ").append(number(y + 27)).append(" H ").append(number(x + 5)).append(" Z M ").append(number(x + 9)).append(" ").append(number(y + 6)).append(" V ").append(number(y + 23)).append("\"").append(stroke).append("/>");
            case "SUNGLASSES_CASE" -> svg.append("<path d=\"M ").append(number(x + 3)).append(" ").append(number(y + 17)).append(" Q ").append(number(x + 8)).append(" ").append(number(y + 7)).append(" ").append(number(x + 14)).append(" ").append(number(y + 17)).append(" Q ").append(number(x + 20)).append(" ").append(number(y + 7)).append(" ").append(number(x + 27)).append(" ").append(number(y + 17)).append(" M ").append(number(x + 14)).append(" ").append(number(y + 17)).append(" H ").append(number(x + 20)).append("\"").append(stroke).append("/>");
            case "USB_C_CHARGER" -> svg.append("<rect x=\"").append(number(x + 7)).append("\" y=\"").append(number(y + 7)).append("\" width=\"15\" height=\"16\" rx=\"3\"").append(stroke).append("/><path d=\"M ").append(number(x + 11)).append(" ").append(number(y + 7)).append(" V ").append(number(y + 3)).append(" M ").append(number(x + 18)).append(" ").append(number(y + 7)).append(" V ").append(number(y + 3)).append("\"").append(stroke).append("/>");
            case "CHARGING_CABLE" -> svg.append("<circle cx=\"").append(number(x + 15)).append("\" cy=\"").append(number(y + 15)).append("\" r=\"9\"").append(stroke).append("/><path d=\"M ").append(number(x + 24)).append(" ").append(number(y + 15)).append(" H ").append(number(x + 29)).append(" V ").append(number(y + 20)).append("\"").append(stroke).append("/>");
            case "EARBUDS_CASE", "CARD_WALLET", "KEY_CASE" -> svg.append("<rect x=\"").append(number(x + 4)).append("\" y=\"").append(number(y + 7)).append("\" width=\"22\" height=\"16\" rx=\"6\"").append(stroke).append("/><path d=\"M ").append(number(x + 5)).append(" ").append(number(y + 13)).append(" H ").append(number(x + 25)).append("\"").append(stroke).append("/>");
            default -> svg.append("<rect x=\"").append(number(x + 3)).append("\" y=\"").append(number(y + 2)).append("\" width=\"24\" height=\"22\" rx=\"5\"").append(stroke).append("/><path d=\"M ").append(number(x + 8)).append(" ").append(number(y + 2)).append(" V ").append(number(y - 2)).append(" H ").append(number(x + 22)).append(" V ").append(number(y + 2)).append("\"").append(stroke).append("/>");
        }
    }

    private void appendItemShape(
            StringBuilder svg,
            PackingCheckResponse.Placement placement,
            double x,
            double y,
            double width,
            double height,
            String color
    ) {
        // A large universal minimum made small accessories look almost as
        // large as tablets and laptops. Keep a tiny rendering floor only so
        // that their physical size relationship remains visible.
        double safeWidth = Math.max(width, 14);
        double safeHeight = Math.max(height, 14);
        String common = " fill=\"" + color + "\" fill-opacity=\"0.40\" stroke=\"" + color + "\" stroke-width=\"2.5\" filter=\"url(#glow)\"";

        String rasterAsset = xrayItemAsset(placement.itemCode());
        if (rasterAsset != null) {
            appendRasterItemShape(
                    svg,
                    placement,
                    x,
                    y,
                    safeWidth,
                    safeHeight,
                    color,
                    rasterAsset
            );
            return;
        }

        switch (placement.itemCode()) {
            case "LAPTOP_13", "LAPTOP_14" -> svg.append("""
                      <rect x="%s" y="%s" width="%s" height="%s" rx="12"%s/>
                      <rect x="%s" y="%s" width="%s" height="%s" rx="6" fill="#07131F" fill-opacity="0.54" stroke="#EAF5FF" stroke-opacity="0.72"/>
                      <rect x="%s" y="%s" width="%s" height="%s" rx="3" fill="#BCE8FF" fill-opacity="0.12"/>
                      <path d="M %s %s H %s M %s %s H %s" stroke="#F3FAFF" stroke-opacity="0.64" stroke-width="2"/>
                    """.formatted(
                    number(x), number(y), number(safeWidth), number(safeHeight), common,
                    number(x + 10), number(y + 10), number(Math.max(12, safeWidth - 20)), number(Math.max(12, safeHeight - 26)),
                    number(x + 15), number(y + 17), number(Math.max(12, safeWidth - 30)), number(Math.max(10, safeHeight * 0.33)),
                    number(x + safeWidth * 0.20), number(y + safeHeight - 8), number(x + safeWidth * 0.80),
                    number(x + safeWidth * 0.42), number(y + safeHeight - 15), number(x + safeWidth * 0.58)
            ));
            case "TABLET_11" -> svg.append("""
                      <rect x="%s" y="%s" width="%s" height="%s" rx="16"%s/>
                      <rect x="%s" y="%s" width="%s" height="%s" rx="10" fill="#0D2539" fill-opacity="0.62" stroke="#E1F2FF" stroke-opacity="0.62"/>
                      <path d="M %s %s H %s M %s %s H %s" stroke="#DBF6FF" stroke-opacity="0.28" stroke-width="1"/>
                      <circle cx="%s" cy="%s" r="4" fill="#E8F4FF" fill-opacity="0.85"/><circle cx="%s" cy="%s" r="1.5" fill="#153B54"/>
                    """.formatted(
                    number(x), number(y), number(safeWidth), number(safeHeight), common,
                    number(x + 8), number(y + 8), number(Math.max(12, safeWidth - 16)), number(Math.max(12, safeHeight - 16)),
                    number(x + 16), number(y + safeHeight * 0.43), number(x + safeWidth - 16),
                    number(x + 16), number(y + safeHeight * 0.62), number(x + safeWidth - 16),
                    number(x + safeWidth - 15), number(y + 15), number(x + safeWidth - 15), number(y + 15)
            ));
            case "SMARTPHONE", "POWER_BANK" -> svg.append("""
                      <rect x="%s" y="%s" width="%s" height="%s" rx="14"%s/>
                      <rect x="%s" y="%s" width="%s" height="%s" rx="9" fill="#091923" fill-opacity="0.60" stroke="#E1F2FF" stroke-opacity="0.60"/>
                      <circle cx="%s" cy="%s" r="5" fill="#CDEEFF" fill-opacity="0.82"/><circle cx="%s" cy="%s" r="2" fill="#17334A"/>
                      <circle cx="%s" cy="%s" r="3" fill="#CDEEFF" fill-opacity="0.70"/><path d="M %s %s H %s" stroke="#D9F3FF" stroke-opacity="0.35"/>
                    """.formatted(
                    number(x), number(y), number(safeWidth), number(safeHeight), common,
                    number(x + 7), number(y + 7), number(Math.max(10, safeWidth - 14)), number(Math.max(10, safeHeight - 14)),
                    number(x + safeWidth / 2), number(y + 16), number(x + safeWidth / 2), number(y + 16),
                    number(x + safeWidth / 2 + 7), number(y + 16), number(x + safeWidth * 0.28), number(y + safeHeight - 12), number(x + safeWidth * 0.72)
            ));
            case "TUMBLER" -> svg.append("""
                      <rect x="%s" y="%s" width="%s" height="%s" rx="%s"%s/>
                      <rect x="%s" y="%s" width="%s" height="%s" rx="5" fill="#DCEBFA" fill-opacity="0.36"/>
                      <path d="M %s %s H %s" stroke="#F1F7FF" stroke-opacity="0.60" stroke-width="2"/>
                    """.formatted(
                    number(x), number(y), number(safeWidth), number(safeHeight), number(Math.min(16, safeWidth / 2)), common,
                    number(x + safeWidth * 0.18), number(y + 5), number(safeWidth * 0.64), number(Math.min(13, safeHeight * 0.16)),
                    number(x + safeWidth * 0.16), number(y + safeHeight * 0.20), number(x + safeWidth * 0.84)
            ));
            case "BOOK_PAPERBACK", "NOTEBOOK_A5", "PASSPORT" -> svg.append("""
                      <path d="M %s %s H %s V %s H %s Z"%s/>
                      <path d="M %s %s V %s M %s %s H %s M %s %s H %s" stroke="#E8F6FF" stroke-opacity="0.68" stroke-width="1.5"/>
                    """.formatted(
                    number(x), number(y), number(x + safeWidth), number(y + safeHeight), number(x), common,
                    number(x + safeWidth * 0.17), number(y + 5), number(y + safeHeight - 5),
                    number(x + safeWidth * 0.30), number(y + safeHeight * 0.34), number(x + safeWidth * 0.82),
                    number(x + safeWidth * 0.30), number(y + safeHeight * 0.57), number(x + safeWidth * 0.75)
            ));
            case "SUNGLASSES_CASE" -> svg.append("""
                      <path d="M %s %s Q %s %s %s %s Q %s %s %s %s"%s/>
                      <path d="M %s %s Q %s %s %s %s Q %s %s %s %s M %s %s H %s" fill="none" stroke="#EAF6FF" stroke-opacity="0.72" stroke-width="2"/>
                    """.formatted(
                    number(x), number(y + safeHeight * 0.58), number(x + safeWidth * 0.25), number(y), number(x + safeWidth * 0.50), number(y + safeHeight * 0.58), number(x + safeWidth * 0.75), number(y), number(x + safeWidth), number(y + safeHeight * 0.58), common,
                    number(x + 4), number(y + safeHeight * 0.56), number(x + safeWidth * 0.25), number(y + safeHeight * 0.20), number(x + safeWidth * 0.48), number(y + safeHeight * 0.56), number(x + safeWidth * 0.73), number(y + safeHeight * 0.20), number(x + safeWidth - 4), number(y + safeHeight * 0.56), number(x + safeWidth * 0.48), number(y + safeHeight * 0.56), number(x + safeWidth * 0.52)
            ));
            case "USB_C_CHARGER" -> svg.append("""
                      <rect x="%s" y="%s" width="%s" height="%s" rx="8"%s/>
                      <path d="M %s %s V %s M %s %s V %s" stroke="#ECF8FF" stroke-opacity="0.78" stroke-width="2"/>
                    """.formatted(
                    number(x), number(y + safeHeight * 0.12), number(safeWidth), number(safeHeight * 0.76), common,
                    number(x + safeWidth * 0.34), number(y + safeHeight * 0.12), number(y),
                    number(x + safeWidth * 0.66), number(y + safeHeight * 0.12), number(y)
            ));
            case "CHARGING_CABLE" -> svg.append("""
                      <circle cx="%s" cy="%s" r="%s"%s/>
                      <circle cx="%s" cy="%s" r="%s" fill="none" stroke="#EAF6FF" stroke-opacity="0.67" stroke-width="2"/>
                      <path d="M %s %s H %s V %s" fill="none" stroke="#EAF6FF" stroke-opacity="0.80" stroke-width="2"/>
                    """.formatted(
                    number(x + safeWidth * 0.44), number(y + safeHeight * 0.50), number(Math.min(safeWidth, safeHeight) * 0.31), common,
                    number(x + safeWidth * 0.44), number(y + safeHeight * 0.50), number(Math.min(safeWidth, safeHeight) * 0.15),
                    number(x + safeWidth * 0.75), number(y + safeHeight * 0.50), number(x + safeWidth * 0.92), number(y + safeHeight * 0.72)
            ));
            case "EARBUDS_CASE", "CARD_WALLET", "KEY_CASE" -> svg.append("""
                      <rect x="%s" y="%s" width="%s" height="%s" rx="%s"%s/>
                      <path d="M %s %s H %s" stroke="#EAF6FF" stroke-opacity="0.75" stroke-width="2"/>
                    """.formatted(
                    number(x), number(y), number(safeWidth), number(safeHeight), number(Math.min(18, safeHeight * 0.35)), common,
                    number(x + safeWidth * 0.10), number(y + safeHeight * 0.46), number(x + safeWidth * 0.90)
            ));
            default -> svg.append("""
                      <rect x="%s" y="%s" width="%s" height="%s" rx="14"%s/>
                      <path d="M %s %s H %s V %s H %s Z" fill="#DCEBFA" fill-opacity="0.25" stroke="#E1F2FF" stroke-opacity="0.40"/>
                    """.formatted(
                    number(x), number(y), number(safeWidth), number(safeHeight), common,
                    number(x + 8), number(y + 8), number(x + safeWidth - 8), number(y + safeHeight - 8), number(x + 8)
            ));
        }

        if (shouldShowItemLabel(placement.itemCode(), safeWidth, safeHeight)) {
            svg.append("<text x=\"").append(number(x + 10)).append("\" y=\"").append(number(y + Math.min(24, safeHeight - 7))).append("\" fill=\"#EDF5FF\" font-family=\"Arial, sans-serif\" font-size=\"12\" font-weight=\"700\">")
                    .append(escape(placement.itemName())).append("</text>");
        }
    }

    private void appendRasterItemShape(
            StringBuilder svg,
            PackingCheckResponse.Placement placement,
            double x,
            double y,
            double width,
            double height,
            String color,
            String assetPath
    ) {
        double scale = rasterScale(placement.itemCode());
        double renderedWidth = width * scale;
        double renderedHeight = height * scale;
        if ("TABLET_11".equals(placement.itemCode())) {
            // The source X-ray cutout is portrait while the catalog's tablet
            // dimensions are landscape (250 x 180 mm). Rotate the artwork to
            // match the physical object instead of fitting it as a miniature
            // upright phone inside a wide tablet footprint.
            double centerX = x + width / 2;
            double centerY = y + height / 2;
            svg.append("""
                      <g filter="url(#glow)" transform="rotate(90 %s %s)">
                        <image href="%s" x="%s" y="%s" width="%s" height="%s" preserveAspectRatio="xMidYMid meet" opacity="0.98" filter="url(#xrayImage)"/>
                      </g>
                    """.formatted(
                    number(centerX), number(centerY), embeddedImageDataUri(assetPath),
                    number(centerX - renderedHeight / 2), number(centerY - renderedWidth / 2),
                    number(renderedHeight), number(renderedWidth)
            ));
            return;
        }
        svg.append("""
                  <g filter="url(#glow)">
                    <image href="%s" x="%s" y="%s" width="%s" height="%s" preserveAspectRatio="xMidYMid meet" opacity="0.96" filter="url(#xrayImage)"/>
                  </g>
                """.formatted(
                embeddedImageDataUri(assetPath), number(x - (renderedWidth - width) / 2), number(y - (renderedHeight - height) / 2), number(renderedWidth), number(renderedHeight)
        ));
    }

    private String xrayItemAsset(String itemCode) {
        return switch (itemCode) {
            case "LAPTOP_13", "LAPTOP_14" -> "/packing-assets/xray-items/laptop-13-xray-cutout.png";
            case "TABLET_11" -> "/packing-assets/xray-items/tablet-11-xray-cutout.png";
            default -> null;
        };
    }

    private boolean shouldShowItemLabel(String itemCode, double width, double height) {
        return (itemCode.startsWith("LAPTOP") || "TABLET_11".equals(itemCode))
                && width >= 90
                && height >= 45;
    }

    private double rasterScale(String itemCode) {
        return switch (itemCode) {
            // These PNGs already include a natural X-ray silhouette. Enlarging
            // them again made laptops visually dominate every bag.
            // The laptop cutout carries generous transparent margins. A
            // modest optical scale restores its real visual dominance without
            // changing the physical fit calculation.
            case "LAPTOP_13", "LAPTOP_14" -> 1.20;
            case "TABLET_11" -> 1.0;
            case "SMARTPHONE" -> 1.0;
            default -> 1.0;
        };
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * The layout engine has a minimum percentage so tiny accessories remain
     * selectable. That minimum is useful for a list, but makes a phone look
     * smaller than a wallet in a rendered X-ray. The scene uses one physical
     * pixels-per-millimetre scale for all everyday items instead.
     */
    private ItemVisualSize physicalVisualSize(
            PackingCheckResponse.Placement placement,
            PackingCheckResponse response,
            PackingZone packingZone
    ) {
        PackingCheckResponse.ItemResult item = response.items().stream()
                .filter(candidate -> candidate.itemCode().equals(placement.itemCode()))
                .findFirst()
                .orElseThrow();
        double pixelsPerMillimetre = Math.min(
                packingZone.width() / response.bagSize().widthMm(),
                packingZone.height() / response.bagSize().heightMm()
        );
        double width = item.widthMm() * pixelsPerMillimetre;
        double height = item.heightMm() * pixelsPerMillimetre;
        double rotatedWidth = height;
        double rotatedHeight = width;
        if (fitRatio(rotatedWidth, rotatedHeight, packingZone)
                < fitRatio(width, height, packingZone)) {
            width = rotatedWidth;
            height = rotatedHeight;
        }
        double downscale = Math.min(
                1.0,
                Math.min(packingZone.width() * 0.94 / width, packingZone.height() * 0.94 / height)
        );
        return new ItemVisualSize(width * downscale, height * downscale);
    }

    private double fitRatio(double width, double height, PackingZone packingZone) {
        return Math.max(width / packingZone.width(), height / packingZone.height());
    }

    /**
     * Product PNGs do not share the same transparent padding. In particular,
     * mini bags have a large blank canvas around the product photo. Rendering
     * the full PNG made a correctly-sized pouch appear outside a tiny bag.
     * Map the visible alpha bounds into the scanner frame instead.
     */
    private ArtworkLayout artworkLayout(String imageUrl, BagFrame frame) {
        ArtworkBounds bounds = artworkBoundsCache.computeIfAbsent(
                imageUrl,
                this::readArtworkBounds
        );
        double targetX = frame.x() + frame.width() * 0.04;
        double targetY = frame.y() + frame.height() * 0.04;
        double targetWidth = frame.width() * 0.92;
        double targetHeight = frame.height() * 0.92;
        double scale = Math.min(
                targetWidth / bounds.contentWidth(),
                targetHeight / bounds.contentHeight()
        );
        double visibleWidth = bounds.contentWidth() * scale;
        double visibleHeight = bounds.contentHeight() * scale;

        return new ArtworkLayout(
                targetX + (targetWidth - visibleWidth) / 2 - bounds.left() * scale,
                targetY + (targetHeight - visibleHeight) / 2 - bounds.top() * scale,
                bounds.sourceWidth() * scale,
                bounds.sourceHeight() * scale
        );
    }

    private ArtworkBounds readArtworkBounds(String imageUrl) {
        try {
            ClassPathResource resource = new ClassPathResource("static" + imageUrl);
            try (InputStream inputStream = resource.getInputStream()) {
                BufferedImage image = ImageIO.read(inputStream);
                if (image == null) {
                    throw new IllegalStateException("지원하지 않는 가방 이미지입니다: " + imageUrl);
                }

                int left = image.getWidth();
                int top = image.getHeight();
                int right = -1;
                int bottom = -1;
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        if (((image.getRGB(x, y) >>> 24) & 0xFF) > 12) {
                            left = Math.min(left, x);
                            top = Math.min(top, y);
                            right = Math.max(right, x);
                            bottom = Math.max(bottom, y);
                        }
                    }
                }
                if (right < left || bottom < top) {
                    return ArtworkBounds.fullImage(image.getWidth(), image.getHeight());
                }
                return new ArtworkBounds(image.getWidth(), image.getHeight(), left, top, right, bottom);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("가방 이미지의 표시 영역을 읽을 수 없습니다: " + imageUrl, exception);
        }
    }

    private String statusColor(PackingStatus status) {
        return switch (status) {
            case COMFORTABLE -> "#7BE495";
            case TIGHT -> "#FFD166";
            case NOT_RECOMMENDED, PROFILE_UNAVAILABLE -> "#FF6B6B";
        };
    }

    private String embeddedImageDataUri(String imageUrl) {
        try {
            ClassPathResource image = new ClassPathResource("static" + imageUrl);
            try (InputStream inputStream = image.getInputStream()) {
                byte[] bytes = inputStream.readAllBytes();
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "엑스레이 미리보기용 가방 이미지를 읽을 수 없습니다: " + imageUrl,
                    exception
            );
        }
    }

    private String statusLabel(PackingStatus status) {
        return switch (status) {
            case COMFORTABLE -> "여유 있음";
            case TIGHT -> "공간 빠듯";
            case NOT_RECOMMENDED -> "일부 물품 제외 필요";
            case PROFILE_UNAVAILABLE -> "분석 불가";
        };
    }

    private String number(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private record BagFrame(
            double x,
            double y,
            double width,
            double height,
            double radius
    ) {

        private static BagFrame from(String scene) {
            return "flight".equals(scene)
                    ? new BagFrame(390, 245, 650, 430, 110)
                    : new BagFrame(465, 125, 510, 630, 145);
        }
    }

    private record ArtworkBounds(
            int sourceWidth,
            int sourceHeight,
            int left,
            int top,
            int right,
            int bottom
    ) {
        private static ArtworkBounds fullImage(int width, int height) {
            return new ArtworkBounds(width, height, 0, 0, width - 1, height - 1);
        }

        private double contentWidth() {
            return right - left + 1;
        }

        private double contentHeight() {
            return bottom - top + 1;
        }
    }

    private record ArtworkLayout(double x, double y, double width, double height) {
    }

    private record ItemVisualSize(double width, double height) {
    }

    private record PackingZone(double x, double y, double width, double height) {
        private static PackingZone forProfile(PackingCheckResponse response, BagFrame frame) {
            return switch (response.loungeId()) {
                // Compact shoppers and crossbodies: the cavity is the body
                // below the opening/handle, never the surrounding strap.
                case "L02", "L05" -> relativeTo(frame, 0.14, 0.40, 0.72, 0.36);
                // Shopper bodies begin below their long handles.
                case "L03" -> relativeTo(frame, 0.12, 0.40, 0.76, 0.46);
                case "L04" -> relativeTo(frame, 0.10, 0.42, 0.80, 0.38);
                // The extra-mini Toni is 16 × 14 cm; its body is compact but
                // a pouch can still be shown neatly inside it.
                case "L07" -> relativeTo(frame, 0.16, 0.39, 0.68, 0.42);
                case "F01", "F02" -> relativeTo(frame, 0.14, 0.36, 0.72, 0.40);
                case "F03", "F04", "F05", "F06", "F07" -> relativeTo(frame, 0.16, 0.42, 0.68, 0.32);
                // These perfume-scene source PNGs have their product near the
                // lower half. Their packing zones follow the real bag body,
                // instead of the blank image canvas.
                case "P01" -> relativeTo(frame, 0.26, 0.43, 0.48, 0.34);
                case "P03" -> relativeTo(frame, 0.08, 0.34, 0.84, 0.40);
                case "P04" -> relativeTo(frame, 0.23, 0.38, 0.54, 0.48);
                case "P05" -> relativeTo(frame, 0.26, 0.52, 0.48, 0.34);
                case "P06" -> relativeTo(frame, 0.16, 0.42, 0.68, 0.32);
                case "P07" -> relativeTo(frame, 0.18, 0.36, 0.64, 0.50);
                default -> relativeTo(frame, 0.23, 0.30, 0.54, 0.54);
            };
        }

        private static PackingZone relativeTo(
                BagFrame frame,
                double x,
                double y,
                double width,
                double height
        ) {
            return new PackingZone(
                    frame.x() + frame.width() * x,
                    frame.y() + frame.height() * y,
                    frame.width() * width,
                    frame.height() * height
            );
        }
    }
}
