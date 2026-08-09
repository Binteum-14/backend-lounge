package com.ddcj.binteum.domain.user.entity;

import com.ddcj.binteum.domain.diagnosis.entity.Diagnosis;
import com.ddcj.binteum.domain.focusrecord.entity.FocusRecord;
import com.ddcj.binteum.domain.recommendation.entity.Recommendation;
import com.ddcj.binteum.domain.visitpass.entity.VisitPass;
import com.ddcj.binteum.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "user")
    private List<FocusRecord> focusRecords = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Diagnosis> diagnoses = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Recommendation> recommendations = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<VisitPass> visitPasses = new ArrayList<>();

    private User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static User create(String username, String encodedPassword) {
        return new User(username, encodedPassword);
    }
}
