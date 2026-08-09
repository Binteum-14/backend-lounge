package com.ddcj.binteum.domain.focusrecord.entity;

import com.ddcj.binteum.domain.flightfocusdetail.entity.FlightFocusDetail;
import com.ddcj.binteum.domain.user.entity.User;
import com.ddcj.binteum.global.entity.CreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "focus_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FocusRecord extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "theme_type")
    private String themeType;

    @Column(name = "all_minutes")
    private Integer allMinutes;

    @Column(name = "study_seconds")
    private Integer studySeconds;

    @Column(name = "break_seconds")
    private Integer breakSeconds;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @OneToOne(mappedBy = "focusRecord", fetch = FetchType.LAZY)
    private FlightFocusDetail flightFocusDetail;
}
