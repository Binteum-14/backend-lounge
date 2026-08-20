package com.lounge.domain.packing.service;

import com.lounge.domain.packing.PackingStatus;
import com.lounge.domain.packing.dto.PackingCheckResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Base64;

/**
 * Renders the analysis result as a browser-ready SVG packing preview.
 */
@Component
public class PackingXrayPreviewRenderer {

    private static final int CANVAS_WIDTH = 1440;
    private static final int CANVAS_HEIGHT = 900;

    private static final String[] ITEM_COLORS = {
            "#F4A340", "#31D6E8", "#F1C94B", "#53BCEB", "#DF7B41", "#78D4BC"
    };

    public String render(PackingCheckResponse response) {
        BagFrame frame = BagFrame.from(response.scene());
        StringBuilder svg = new StringBuilder();

        svg.append("""
                <svg xmlns="http://www.w3.org/2000/svg" width="1440" height="900" viewBox="0 0 1440 900" role="img">
                  <defs>
                    <linearGradient id="background" x1="0" x2="0" y1="0" y2="1">
                      <stop stop-color="#05080D"/><stop offset="0.52" stop-color="#08131E"/><stop offset="1" stop-color="#03070B"/>
                    </linearGradient>
                    <linearGradient id="scan" x1="0" x2="0" y1="0" y2="1">
                      <stop stop-color="#FFFFFF" stop-opacity="0"/>
                      <stop offset="0.47" stop-color="#B8E6FF" stop-opacity="0.08"/>
                      <stop offset="0.50" stop-color="#F2FBFF" stop-opacity="0.52"/>
                      <stop offset="0.53" stop-color="#86C9FF" stop-opacity="0.10"/>
                      <stop offset="1" stop-color="#FFFFFF" stop-opacity="0"/>
                    </linearGradient>
                    <linearGradient id="bagGlow" x1="0" x2="1" y1="0" y2="1">
                      <stop stop-color="#A8D7FF" stop-opacity="0.55"/><stop offset="0.5" stop-color="#4A79B5" stop-opacity="0.18"/><stop offset="1" stop-color="#8EB5E9" stop-opacity="0.42"/>
                    </linearGradient>
                    <pattern id="grid" width="28" height="28" patternUnits="userSpaceOnUse">
                      <path d="M 28 0 L 0 0 0 28" fill="none" stroke="#64809F" stroke-opacity="0.15" stroke-width="1"/>
                    </pattern>
                    <filter id="glow"><feGaussianBlur stdDeviation="5" result="blur"/><feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge></filter>
                    <filter id="softGlow"><feGaussianBlur stdDeviation="12"/></filter>
                    <filter id="xrayImage"><feColorMatrix type="matrix" values="0.25 0.10 0.15 0 0.18 0.10 0.36 0.22 0 0.34 0.10 0.18 0.48 0 0.55 0 0 0 0.75 0"/></filter>
                    <clipPath id="bagClip"><rect x="%s" y="%s" width="%s" height="%s" rx="%s"/></clipPath>
                    <mask id="bagShapeMask" maskUnits="userSpaceOnUse" x="%s" y="%s" width="%s" height="%s" style="mask-type:alpha">
                      <image href="%s" x="%s" y="%s" width="%s" height="%s" preserveAspectRatio="xMidYMid meet"/>
                    </mask>
                  </defs>
                  <rect width="1440" height="900" fill="url(#background)"/>
                  <rect width="1440" height="900" fill="url(#grid)"/>
                  <circle cx="720" cy="410" r="390" fill="#2C94C7" fill-opacity="0.10" filter="url(#softGlow)"/>
                  <rect x="38" y="28" width="1364" height="72" rx="12" fill="#080E15" fill-opacity="0.92" stroke="#32485C" stroke-width="1.5"/>
                  <circle cx="76" cy="64" r="11" fill="#37D8BE" filter="url(#glow)"/><circle cx="76" cy="64" r="4" fill="#E7FFF9"/>
                  <text x="102" y="58" fill="#EAF1FA" font-family="Arial, sans-serif" font-size="24" font-weight="800" letter-spacing="1">AI PACKING · SECURITY SCREENING</text>
                  <text x="103" y="81" fill="#7E94AB" font-family="Arial, sans-serif" font-size="12" letter-spacing="1.4">X-RAY ANALYSIS / CLEARANCE MODE</text>
                  <text x="1364" y="59" fill="#B8CBDD" text-anchor="end" font-family="Arial, sans-serif" font-size="14" font-weight="700">LANE 04 · ONLINE</text>
                  <text x="1364" y="81" fill="#6E879E" text-anchor="end" font-family="Arial, sans-serif" font-size="12">%s</text>
                """.formatted(
                number(frame.x()), number(frame.y()), number(frame.width()), number(frame.height()), number(frame.radius()),
                number(frame.x()), number(frame.y()), number(frame.width()), number(frame.height()),
                embeddedImageDataUri(response.imageUrl()), number(frame.x()), number(frame.y()), number(frame.width()), number(frame.height()),
                escape(response.productName())
        ));

        appendSidebar(svg, response);
        appendBag(svg, response, frame);
        appendItems(svg, response.placements(), frame);
        appendScanOverlay(svg, frame);
        appendResultPanel(svg, response);
        appendSpaceUsed(svg, response);
        svg.append("</svg>");

        return svg.toString();
    }

    private void appendSidebar(StringBuilder svg, PackingCheckResponse response) {
        svg.append("""
                  <rect x="58" y="132" width="270" height="548" rx="16" fill="#080E15" fill-opacity="0.93" stroke="#31465A" stroke-width="1.5"/>
                  <rect x="58" y="132" width="270" height="38" rx="16" fill="#142333"/><path d="M58 154 H328" stroke="#31465A"/>
                  <text x="86" y="157" fill="#B8CDDD" font-family="Arial, sans-serif" font-size="12" font-weight="800" letter-spacing="1.2">SCREENING MANIFEST</text>
                  <text x="298" y="157" fill="#E5EDF8" text-anchor="end" font-family="Arial, sans-serif" font-size="13" font-weight="700">%d / %d</text>
                  <text x="86" y="197" fill="#70889F" font-family="Arial, sans-serif" font-size="11" letter-spacing="1">DETECTED CONTENTS</text>
                """.formatted(response.placements().size(), response.items().size()));

        for (int index = 0; index < response.items().size(); index++) {
            PackingCheckResponse.ItemResult item = response.items().get(index);
            double y = 232 + index * 68;
            String color = item.fit() ? "#B6C8E6" : "#657487";
            svg.append("<g opacity=\"").append(item.fit() ? "1" : "0.48").append("\">");
            appendItemIcon(svg, item.itemCode(), 88, y - 18, color);
            svg.append("""
                      <text x="132" y="%s" fill="#E4ECF7" font-family="Arial, sans-serif" font-size="15" font-weight="600">%s</text>
                      <text x="132" y="%s" fill="#7F91A8" font-family="Arial, sans-serif" font-size="12">%s</text>
                      <rect x="277" y="%s" width="20" height="20" rx="4" fill="%s"/>
                      <text x="287" y="%s" text-anchor="middle" fill="#08111B" font-family="Arial, sans-serif" font-size="15" font-weight="800">%s</text>
                    """.formatted(
                    number(y - 2), escape(item.itemName()), number(y + 17), escape(item.itemCode()),
                    number(y - 18), item.fit() ? "#A9C3EF" : "#3A4655", number(y - 3), item.fit() ? "✓" : "–"
            ));
            svg.append("</g>");
        }
    }

    private void appendBag(
            StringBuilder svg,
            PackingCheckResponse response,
            BagFrame frame
    ) {
        svg.append("""
                  <g>
                    <rect x="390" y="110" width="660" height="658" rx="42" fill="#07111B" fill-opacity="0.92" stroke="#3E5870" stroke-width="2"/>
                    <rect x="409" y="130" width="622" height="620" rx="28" fill="#102536" stroke="#5B7894" stroke-width="1.5"/>
                    <path d="M420 161 H1020 M420 720 H1020" stroke="#78A1C4" stroke-opacity="0.38"/>
                    <text x="438" y="157" fill="#8DAAC2" font-family="Arial, sans-serif" font-size="11" font-weight="700" letter-spacing="1.5">BAGGAGE SCANNER · X-RAY VIEW</text>
                    <text x="1005" y="157" fill="#44DBD0" text-anchor="end" font-family="Arial, sans-serif" font-size="11" font-weight="800">CLEAR</text>
                    <rect x="%s" y="%s" width="%s" height="%s" rx="%s" fill="#50B7E8" fill-opacity="0.05" filter="url(#softGlow)"/>
                    <rect x="%s" y="%s" width="%s" height="%s" rx="%s" fill="#4A7FB0" fill-opacity="0.02" stroke="#91C8F0" stroke-opacity="0.16" stroke-width="1"/>
                    <image href="%s" x="%s" y="%s" width="%s" height="%s" opacity="0.62" preserveAspectRatio="xMidYMid meet" filter="url(#xrayImage)"/>
                    <rect x="%s" y="%s" width="%s" height="%s" rx="%s" fill="url(#bagGlow)" fill-opacity="0.48" mask="url(#bagShapeMask)"/>
                    <rect x="%s" y="%s" width="%s" height="%s" rx="%s" fill="none" stroke="#C9E0FF" stroke-opacity="0" stroke-width="2"/>
                    <rect x="%s" y="%s" width="%s" height="28" fill="#C3EEFF" fill-opacity="0.08" mask="url(#bagShapeMask)"/>
                    <path d="M %s %s H %s M %s %s H %s" stroke="#B7D9FF" stroke-opacity="0.50" stroke-width="1"/>
                    <path d="M 360 774 H1080" stroke="#192C3D" stroke-width="26"/><path d="M 360 774 H1080" stroke="#617A91" stroke-opacity="0.70" stroke-width="2" stroke-dasharray="12 10"/>
                    <path d="M 422 774 H1018" stroke="#B7D8EC" stroke-opacity="0.32" stroke-width="2"/>
                  </g>
                """.formatted(
                number(frame.x()), number(frame.y()), number(frame.width()), number(frame.height()), number(frame.radius()),
                number(frame.x()), number(frame.y()), number(frame.width()), number(frame.height()), number(frame.radius()),
                embeddedImageDataUri(response.imageUrl()), number(frame.x()), number(frame.y()), number(frame.width()), number(frame.height()),
                number(frame.x()), number(frame.y()), number(frame.width()), number(frame.height()), number(frame.radius()),
                number(frame.x()), number(frame.y()), number(frame.width()), number(frame.height()), number(frame.radius()),
                number(frame.x()), number(frame.y() + frame.height() * 0.48), number(frame.width()),
                number(frame.x() + 28), number(frame.y() + 36), number(frame.x() + frame.width() - 28),
                number(frame.x() + 28), number(frame.y() + frame.height() - 36), number(frame.x() + frame.width() - 28)
        ));
    }

    private void appendItems(
            StringBuilder svg,
            List<PackingCheckResponse.Placement> placements,
            BagFrame frame
    ) {
        if (placements.isEmpty()) {
            return;
        }

        svg.append("<g mask=\"url(#bagShapeMask)\">");
        double minX = 100;
        double minY = 100;
        double maxX = 0;
        double maxY = 0;
        for (PackingCheckResponse.Placement placement : placements) {
            minX = Math.min(minX, placement.x());
            minY = Math.min(minY, placement.y());
            maxX = Math.max(maxX, placement.x() + placement.width());
            maxY = Math.max(maxY, placement.y() + placement.height());
        }

        // Centre the entire selected manifest and expand it as one group. The
        // same scale is used in both axes so a laptop never loses its physical
        // relationship to a book, tablet, or small accessory.
        double groupWidth = Math.max(1, maxX - minX);
        double groupHeight = Math.max(1, maxY - minY);
        // Keep every selected item inside the backpack's wide central area.
        // X-ray contents may overlap a little, but they must never be cut by
        // the tapered shoulders of the bag silhouette.
        double groupScale = Math.min(88 / groupWidth, 86 / groupHeight);
        double weightedCenterX = 0;
        double weightedCenterY = 0;
        double totalVisualArea = 0;
        for (PackingCheckResponse.Placement placement : placements) {
            double visualArea = placement.width() * placement.height();
            weightedCenterX += ((placement.x() - minX) + placement.width() / 2) * groupScale * visualArea;
            weightedCenterY += ((placement.y() - minY) + placement.height() / 2) * groupScale * visualArea;
            totalVisualArea += visualArea;
        }

        // Centre the visual mass, not merely the outer bounding rectangle. A
        // laptop and tablet carry much more visual weight than earphones, so
        // this moves the entire group until those large objects sit centrally.
        double offsetX = clamp(50 - weightedCenterX / totalVisualArea, 11, 89 - groupWidth * groupScale);
        double offsetY = clamp(50 - weightedCenterY / totalVisualArea, 10, 90 - groupHeight * groupScale);

        double innerX = frame.x() + frame.width() * 0.16;
        double innerY = frame.y() + frame.height() * 0.12;
        double innerWidth = frame.width() * 0.68;
        double innerHeight = frame.height() * 0.78;
        PackingCheckResponse.Placement primaryItem = placements.get(0);
        for (PackingCheckResponse.Placement placement : placements) {
            if (placement.width() * placement.height()
                    > primaryItem.width() * primaryItem.height()) {
                primaryItem = placement;
            }
        }
        for (int index = 0; index < placements.size(); index++) {
            PackingCheckResponse.Placement placement = placements.get(index);
            String color = ITEM_COLORS[index % ITEM_COLORS.length];
            double x = innerX + innerWidth * (offsetX + (placement.x() - minX) * groupScale) / 100;
            double y = innerY + innerHeight * (offsetY + (placement.y() - minY) * groupScale) / 100;
            double width = innerWidth * placement.width() * groupScale / 100;
            double height = innerHeight * placement.height() * groupScale / 100;

            // The dominant item (usually the laptop) is the visual anchor of
            // the X-ray. Put that item on the scanner's true vertical centre
            // line, then arrange the remaining items around it.
            if (placement == primaryItem) {
                x = frame.x() + (frame.width() - width) / 2;
            }

            appendItemShape(svg, placement, x, y, width, height, color);
        }
        svg.append("</g>");
    }

    private void appendScanOverlay(StringBuilder svg, BagFrame frame) {
        double centerX = frame.x() + frame.width() / 2;
        svg.append("""
                  <g mask="url(#bagShapeMask)" pointer-events="none">
                    <rect x="%s" y="%s" width="%s" height="4" fill="#D5F5FF" fill-opacity="0.74" filter="url(#glow)">
                      <animate attributeName="y" values="%s;%s;%s" dur="2.8s" repeatCount="indefinite"/>
                    </rect>
                    <rect x="%s" y="%s" width="%s" height="46" fill="#B3E8FF" fill-opacity="0.06">
                      <animate attributeName="y" values="%s;%s;%s" dur="2.8s" repeatCount="indefinite"/>
                    </rect>
                    <path d="M %s %s V %s M %s %s H %s" stroke="#DCF6FF" stroke-opacity="0.48" stroke-width="1.5"/>
                    <circle cx="%s" cy="%s" r="8" fill="none" stroke="#DCF6FF" stroke-opacity="0.48"/>
                    <text x="%s" y="%s" fill="#C7E8F8" fill-opacity="0.62" text-anchor="middle" font-family="Arial, sans-serif" font-size="10" letter-spacing="1.6">MATERIAL DENSITY SCAN</text>
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
        double usedWidth = 196 * Math.min(1, response.usedSpaceRatio());
        double fitWidth = 196 * response.fitScore() / 100.0;
        svg.append("""
                  <rect x="1102" y="132" width="280" height="548" rx="16" fill="#080E15" fill-opacity="0.93" stroke="#31465A" stroke-width="1.5"/>
                  <rect x="1102" y="132" width="280" height="38" rx="16" fill="#142333"/><path d="M1102 154 H1382" stroke="#31465A"/>
                  <text x="1130" y="157" fill="#B8CDDD" font-family="Arial, sans-serif" font-size="12" font-weight="800" letter-spacing="1.2">SCREENING RESULT</text>
                  <text x="1130" y="218" fill="#E4ECF7" font-family="Arial, sans-serif" font-size="18" font-weight="700">%s</text>
                  <line x1="1130" y1="240" x2="1354" y2="240" stroke="#263444"/>
                  <text x="1130" y="284" fill="#8698AD" font-family="Arial, sans-serif" font-size="13" font-weight="700">수납률</text>
                  <text x="1130" y="326" fill="#C5D9F5" font-family="Arial, sans-serif" font-size="40" font-weight="500">%.0f%%</text>
                  <rect x="1130" y="347" width="196" height="7" rx="3.5" fill="#202B38"/><rect x="1130" y="347" width="%s" height="7" rx="3.5" fill="#8AA8D3"/>
                  <text x="1130" y="405" fill="#8698AD" font-family="Arial, sans-serif" font-size="13" font-weight="700">예상 적합도</text>
                  <text x="1130" y="447" fill="#C5D9F5" font-family="Arial, sans-serif" font-size="40" font-weight="500">%d%%</text>
                  <rect x="1130" y="468" width="196" height="7" rx="3.5" fill="#202B38"/><rect x="1130" y="468" width="%s" height="7" rx="3.5" fill="#8AA8D3"/>
                  <line x1="1130" y1="518" x2="1354" y2="518" stroke="#263444"/>
                  <text x="1130" y="555" fill="#8698AD" font-family="Arial, sans-serif" font-size="13" font-weight="700">CLEARANCE</text>
                  <circle cx="1141" cy="588" r="6" fill="%s"/><text x="1158" y="594" fill="#DFEAF7" font-family="Arial, sans-serif" font-size="15" font-weight="600">%s</text>
                  <text x="1130" y="630" fill="#7F91A8" font-family="Arial, sans-serif" font-size="13">%s</text>
                """.formatted(
                escape(response.productName()), response.usedSpaceRatio() * 100, number(usedWidth),
                response.fitScore(), number(fitWidth), statusColor, escape(statusLabel(response.status())),
                escape(response.scene().equals("flight") ? "Flight baggage profile" : "Lounge baggage profile")
        ));
    }

    private void appendSpaceUsed(StringBuilder svg, PackingCheckResponse response) {
        double progress = Math.min(1, response.usedSpaceRatio());
        StringBuilder bars = new StringBuilder();
        for (int index = 0; index < 20; index++) {
            bars.append("<rect x=\"").append(464 + index * 22).append("\" y=\"817\" width=\"14\" height=\"18\" rx=\"2\" fill=\"")
                    .append(index < Math.ceil(progress * 20) ? "#9BB8E8" : "#202B38").append("\"/>");
        }
        svg.append("""
                  <rect x="430" y="777" width="580" height="92" rx="20" fill="#0B1018" fill-opacity="0.88" stroke="#253140" stroke-width="1.5"/>
                  <text x="464" y="808" fill="#92A3B8" font-family="Arial, sans-serif" font-size="13" font-weight="700">SPACE USED</text>
                  <text x="976" y="808" text-anchor="end" fill="#C5D9F5" font-family="Arial, sans-serif" font-size="31" font-weight="500">%.0f%%</text>
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
            case "LAPTOP_13", "LAPTOP_15" -> svg.append("<rect x=\"").append(number(x)).append("\" y=\"").append(number(y)).append("\" width=\"28\" height=\"19\" rx=\"2\"").append(stroke).append("/><path d=\"M ").append(number(x - 4)).append(" ").append(number(y + 23)).append(" H ").append(number(x + 32)).append("\"").append(stroke).append("/>");
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
        double safeWidth = Math.max(width, 36);
        double safeHeight = Math.max(height, 36);
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
            case "LAPTOP_13", "LAPTOP_15" -> svg.append("""
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
        svg.append("""
                  <g filter="url(#glow)">
                    <image href="%s" x="%s" y="%s" width="%s" height="%s" preserveAspectRatio="xMidYMid meet" opacity="0.96"/>
                  </g>
                """.formatted(
                embeddedImageDataUri(assetPath), number(x - (renderedWidth - width) / 2), number(y - (renderedHeight - height) / 2), number(renderedWidth), number(renderedHeight)
        ));
    }

    private String xrayItemAsset(String itemCode) {
        return switch (itemCode) {
            case "LAPTOP_13", "LAPTOP_15" -> "/packing-assets/xray-items/laptop-13-xray-cutout.png";
            case "TABLET_11" -> "/packing-assets/xray-items/tablet-11-xray-cutout.png";
            case "SMARTPHONE" -> "/packing-assets/xray-items/smartphone-xray-cutout.png";
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
            case "LAPTOP_13", "LAPTOP_15" -> 1.25;
            case "TABLET_11" -> 1.05;
            case "SMARTPHONE" -> 1.0;
            default -> 1.0;
        };
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
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
            case NOT_RECOMMENDED -> "수납 비추천";
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
}
