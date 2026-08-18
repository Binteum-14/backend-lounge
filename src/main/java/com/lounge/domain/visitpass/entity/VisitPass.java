package com.lounge.domain.visitpass.entity;

import com.lounge.domain.recommendationproduct.entity.RecommendationProduct;
import com.lounge.domain.user.entity.User;
import com.lounge.domain.visitpassproduct.entity.VisitPassProduct;
import com.lounge.global.entity.CreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "visit_pass")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VisitPass extends CreatedAtEntity {

    private static final String STATUS_ISSUED = "ISSUED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommendation_product_id", nullable = false)
    private RecommendationProduct recommendationProduct;

    @Column(name = "public_token", nullable = false, unique = true)
    private String publicToken;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "qr_code_url")
    private String qrCodeUrl;

    private String status;

    @OneToMany(mappedBy = "visitPass")
    private List<VisitPassProduct> visitPassProducts = new ArrayList<>();

    private VisitPass(
            User user,
            RecommendationProduct recommendationProduct,
            String publicToken,
            LocalDate issuedDate,
            String qrCodeUrl
    ) {
        this.user = user;
        this.recommendationProduct = recommendationProduct;
        this.publicToken = publicToken;
        this.issuedDate = issuedDate;
        this.qrCodeUrl = qrCodeUrl;
        this.status = STATUS_ISSUED;
    }

    public static VisitPass create(
            User user,
            RecommendationProduct recommendationProduct,
            String publicToken,
            LocalDate issuedDate,
            String qrCodeUrl
    ) {
        return new VisitPass(
                user,
                recommendationProduct,
                publicToken,
                issuedDate,
                qrCodeUrl
        );
    }
}
