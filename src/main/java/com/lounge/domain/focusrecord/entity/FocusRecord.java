package com.lounge.domain.focusrecord.entity;

import com.lounge.domain.flightfocusdetail.entity.FlightFocusDetail;
import com.lounge.domain.user.entity.User;
import com.lounge.global.entity.CreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "theme_type")
    private FocusThemeType themeType;

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

    private FocusRecord(
            User user,
            FocusThemeType themeType,
            Integer allMinutes,
            Integer studySeconds,
            Integer breakSeconds,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
        this.user = user;
        this.themeType = themeType;
        this.allMinutes = allMinutes;
        this.studySeconds = studySeconds;
        this.breakSeconds = breakSeconds;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public static FocusRecord create(
            User user,
            FocusThemeType themeType,
            Integer allMinutes,
            Integer studySeconds,
            Integer breakSeconds,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
        return new FocusRecord(
                user,
                themeType,
                allMinutes,
                studySeconds,
                breakSeconds,
                startedAt,
                endedAt
        );
    }
}
