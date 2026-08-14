package com.lounge.domain.user.entity;

import com.lounge.domain.diagnosis.entity.Diagnosis;
import com.lounge.domain.focusrecord.entity.FocusRecord;
import com.lounge.domain.recommendation.entity.Recommendation;
import com.lounge.domain.visitpass.entity.VisitPass;
import com.lounge.global.entity.BaseEntity;
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
