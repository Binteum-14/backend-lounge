package com.ddcj.binteum.domain.visitpass.entity;

import com.ddcj.binteum.domain.user.entity.User;
import com.ddcj.binteum.domain.visitpassproduct.entity.VisitPassProduct;
import com.ddcj.binteum.global.entity.CreatedAtEntity;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "pass_code", unique = true)
    private String passCode;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "qr_code_url")
    private String qrCodeUrl;

    private String status;

    @OneToMany(mappedBy = "visitPass")
    private List<VisitPassProduct> visitPassProducts = new ArrayList<>();
}
